package com.account.account_ms.controller;
import com.account.account_ms.entity.CuentaEntity;
import com.account.account_ms.exceptions.ClienteNoEncontradoException;
import com.account.account_ms.exceptions.CuentaNoEncontradaException;
import com.account.account_ms.exceptions.SaldoInsuficienteException;
import com.account.account_ms.exceptions.ValidacionCuentaException;
import com.account.account_ms.model.CuentaRequest;
import com.account.account_ms.model.CuentaResponse;
import com.account.account_ms.model.CuentasCuentaIdDepositarPutRequest;
import com.account.account_ms.model.CuentasCuentaIdRetirarPutRequest;
import com.account.account_ms.repository.CuentaRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CuentasApiDelegateImpl implements CuentasApiDelegate {
    private final CuentaRepository cuentaRepository;
    private final RestTemplate restTemplate;

    public CuentasApiDelegateImpl(CuentaRepository cuentaRepository, RestTemplate restTemplate) {
        this.cuentaRepository = cuentaRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<CuentaResponse> cuentasPost(@Valid @RequestBody CuentaRequest cuentaRequest) {

        if (cuentaRequest.getSaldoInicial() <= 0) {
            throw new ValidacionCuentaException("El saldo inicial debe ser mayor a 0.");
        }

        try {
            String clienteUrl = "http://localhost:8080/clientes/" + cuentaRequest.getClienteId();

            ResponseEntity<Void> response = restTemplate.exchange(
                    clienteUrl,
                    HttpMethod.GET,
                    null,
                    Void.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ClienteNoEncontradoException("Cliente con ID " + cuentaRequest.getClienteId() + " no encontrado.");
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ClienteNoEncontradoException("Cliente con ID " + cuentaRequest.getClienteId() + " no encontrado.");
        }


        CuentaEntity nuevaCuenta = new CuentaEntity();
        nuevaCuenta.setClienteId(cuentaRequest.getClienteId());
        nuevaCuenta.setTipoCuenta(CuentaEntity.TipoCuentaEnum.valueOf(cuentaRequest.getTipoCuenta().toString()));
        nuevaCuenta.setSaldo(cuentaRequest.getSaldoInicial());
        nuevaCuenta.setNumeroCuenta(UUID.randomUUID().toString().substring(0, 10).toUpperCase());

        CuentaEntity cuentaGuardada = cuentaRepository.save(nuevaCuenta);

        return new ResponseEntity<>(mapToCuentaResponse(cuentaGuardada), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<CuentaResponse>> cuentasGet() {
        List<CuentaEntity> cuentas = cuentaRepository.findAll();
        if (cuentas.isEmpty()) {
            throw new CuentaNoEncontradaException("No se encontraron cuentas en el sistema.");
        }
        List<CuentaResponse> responseList = cuentas.stream()
                .map(this::mapToCuentaResponse)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CuentaResponse> cuentasIdGet(Long id) {
        Optional<CuentaEntity> cuentaOptional = cuentaRepository.findById(id);
        if (cuentaOptional.isEmpty()) {
            throw new CuentaNoEncontradaException("Cuenta con ID " + id + " no encontrada.");
        }
        return new ResponseEntity<>(mapToCuentaResponse(cuentaOptional.get()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> cuentasIdDelete(Long id) {
        Optional<CuentaEntity> cuentaOptional = cuentaRepository.findById(id);
        if (cuentaOptional.isEmpty()) {
            throw new CuentaNoEncontradaException("Cuenta con ID " + id + " no encontrada.");
        }

        CuentaEntity cuenta = cuentaOptional.get();
        if (cuenta.getSaldo() != 0.0) {
            throw new SaldoInsuficienteException("No se puede eliminar la cuenta con ID " + id + " porque su saldo no es 0.0.");
        }

        cuentaRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<CuentaResponse> cuentasCuentaIdDepositarPut(Long cuentaId, CuentasCuentaIdDepositarPutRequest body) {
        Optional<CuentaEntity> cuentaOptional = cuentaRepository.findById(cuentaId);
        if (cuentaOptional.isEmpty()) {
            throw new CuentaNoEncontradaException("Cuenta con ID " + cuentaId + " no encontrada.");
        }
        if (body.getMonto() <= 0) {
            throw new ValidacionCuentaException("El monto a depositar debe ser mayor a 0.");
        }
        CuentaEntity cuenta = cuentaOptional.get();
        cuenta.setSaldo(cuenta.getSaldo() + body.getMonto());
        CuentaEntity cuentaActualizada = cuentaRepository.save(cuenta);
        return new ResponseEntity<>(mapToCuentaResponse(cuentaActualizada), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CuentaResponse> cuentasCuentaIdRetirarPut(Long cuentaId, CuentasCuentaIdRetirarPutRequest body) {
        Optional<CuentaEntity> cuentaOptional = cuentaRepository.findById(cuentaId);
        if (cuentaOptional.isEmpty()) {
            throw new CuentaNoEncontradaException("Cuenta con ID " + cuentaId + " no encontrada.");
        }
        if (body.getMonto() <= 0) {
            throw new ValidacionCuentaException("El monto a retirar debe ser mayor a 0.");
        }
        CuentaEntity cuenta = cuentaOptional.get();

        if (cuenta.getTipoCuenta() == CuentaEntity.TipoCuentaEnum.AHORROS) {
            if (cuenta.getSaldo() - body.getMonto() < 0) {
                throw new SaldoInsuficienteException("Saldo insuficiente para cuenta de ahorros.");
            }
        } else if (cuenta.getTipoCuenta() == CuentaEntity.TipoCuentaEnum.CORRIENTE) {
            if (cuenta.getSaldo() - body.getMonto() < -500.0) {
                throw new SaldoInsuficienteException("El sobregiro de la cuenta corriente no puede exceder los $500.");
            }
        }

        cuenta.setSaldo(cuenta.getSaldo() - body.getMonto());
        CuentaEntity cuentaActualizada = cuentaRepository.save(cuenta);
        return new ResponseEntity<>(mapToCuentaResponse(cuentaActualizada), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CuentaResponse>> cuentasClienteClienteIdGet(Long clienteId) {

        List<CuentaEntity> cuentas = cuentaRepository.findByClienteId(clienteId);


        if (cuentas.isEmpty()) {
            return new ResponseEntity<>(cuentas.stream().map(this::mapToCuentaResponse).collect(Collectors.toList()), HttpStatus.OK);
        }

        List<CuentaResponse> responseList = cuentas.stream().map(this::mapToCuentaResponse).collect(Collectors.toList());
        return new ResponseEntity<>(responseList, HttpStatus.OK);
    }

    private CuentaResponse mapToCuentaResponse(CuentaEntity entity) {
        CuentaResponse response = new CuentaResponse();
        response.setId(entity.getId());
        response.setNumeroCuenta(entity.getNumeroCuenta());
        response.setSaldo(entity.getSaldo());
        response.setTipoCuenta(com.account.account_ms.model.TipoCuenta.fromValue(entity.getTipoCuenta().toString()));
        response.setClienteId(entity.getClienteId());
        return response;
    }
}
