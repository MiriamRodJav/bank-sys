package com.xyz.bank.customer_ms.repository;

import com.xyz.bank.customer_ms.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByDni(String dni);

    boolean existsByDni(String dni);

}