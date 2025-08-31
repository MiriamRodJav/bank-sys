package xyz.bank.customerms.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.bank.customerms.domain.Cliente;

import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByDni(String dni);

    Optional<Cliente> findById(UUID id);
}

