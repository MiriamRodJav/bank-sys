package xyz.bank.accountms.service;

import com.xyzbank.api.model.CuentaCreate;
import com.xyzbank.api.model.Movimiento;
import org.springframework.stereotype.Service;
import xyz.bank.accountms.config.CustomerClient;
import xyz.bank.accountms.domain.Cuenta;
import xyz.bank.accountms.domain.TipoCuenta;
import xyz.bank.accountms.repo.CuentaRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private final CuentaRepository repo;
    private final CustomerClient customerClient;

    public CuentaService(CuentaRepository repo, CustomerClient customerClient) {
        this.repo = repo;
        this.customerClient = customerClient;
    }

    private static BigDecimal toAmount(Double d) {
        return (d == null) ? null : BigDecimal.valueOf(d).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static void requirePositive(BigDecimal m) {
        if (m == null || m.signum() <= 0) throw new Validation("Monto inválido");
    }

    public List<com.xyzbank.api.model.Cuenta> list(UUID clienteId) {
        var list = (clienteId == null) ? repo.findAll() : repo.findByClienteId(clienteId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public com.xyzbank.api.model.Cuenta get(UUID id) {
        var c = repo.findById(id).orElseThrow(() -> new NotFound("Cuenta no encontrada"));
        return toDto(c);
    }

    @Transactional
    public com.xyzbank.api.model.Cuenta create(CuentaCreate in) {
        var clienteId = in.getClienteId();
        if (!customerClient.customerExists(clienteId))
            throw new NotFound("Cliente no existe (CustomerMs)");

        var saldoInicial = toAmount(in.getSaldoInicial());
        if (saldoInicial.compareTo(BigDecimal.ZERO) <= 0)
            throw new Validation("El saldoInicial debe ser mayor a 0.00");

        var c = new Cuenta();
        c.setClienteId(clienteId);
        c.setTipoCuenta(TipoCuenta.valueOf(in.getTipoCuenta().getValue()));
        c.setSaldo(saldoInicial);
        c.setNumeroCuenta(generaNumeroCuentaUnico());
        c = repo.save(c);
        return toDto(c);
    }

    @Transactional
    public com.xyzbank.api.model.Cuenta depositar(UUID cuentaId, Movimiento mov) {
        var monto = toAmount(mov.getMonto());
        requirePositive(monto);
        var c = repo.findById(cuentaId).orElseThrow(() -> new NotFound("Cuenta no encontrada"));
        c.setSaldo(c.getSaldo().add(monto));
        return toDto(repo.save(c));
    }

    // ---------- helpers ----------

    @Transactional
    public com.xyzbank.api.model.Cuenta retirar(UUID cuentaId, Movimiento mov) {
        var monto = toAmount(mov.getMonto());
        requirePositive(monto);
        var c = repo.findById(cuentaId).orElseThrow(() -> new NotFound("Cuenta no encontrada"));
        var nuevo = c.getSaldo().subtract(monto);

        var limite = (c.getTipoCuenta() == TipoCuenta.AHORROS)
                ? BigDecimal.ZERO
                : new BigDecimal("-500.00");

        if (nuevo.compareTo(limite) < 0) {
            throw new Validation("Fondos insuficientes según reglas de tipo " + c.getTipoCuenta());
        }
        c.setSaldo(nuevo);
        return toDto(repo.save(c));
    }

    @Transactional
    public void delete(UUID id) {
        var c = repo.findById(id).orElseThrow(() -> new NotFound("Cuenta no encontrada"));
        repo.delete(c);
    }

    private String generaNumeroCuentaUnico() {
        // 14 dígitos pseudo-únicos. Asegura unicidad por repositorio.
        String n;
        do {
            n = String.format("%014d", Math.abs(ThreadLocalRandom.current().nextLong()) % 100_000_000_000_000L);
        } while (repo.existsByNumeroCuenta(n));
        return n;
    }

    private com.xyzbank.api.model.Cuenta toDto(Cuenta c) {
        var dto = new com.xyzbank.api.model.Cuenta();
        dto.setId(c.getId());
        dto.setNumeroCuenta(c.getNumeroCuenta());
        dto.setSaldo(c.getSaldo().doubleValue());
        dto.setClienteId(c.getClienteId());
        dto.setTipoCuenta(com.xyzbank.api.model.TipoCuenta.valueOf(c.getTipoCuenta().name()));
        return dto;
    }

    // Excepciones de dominio simples
    public static class NotFound extends RuntimeException {
        public NotFound(String m) {
            super(m);
        }
    }

    public static class Validation extends RuntimeException {
        public Validation(String m) {
            super(m);
        }
    }
}
