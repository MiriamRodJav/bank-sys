package xyz.bank.accountms.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.bank.accountms.domain.Cuenta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {
    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(UUID clienteId);

    Optional<Cuenta> findById(UUID id);
}
