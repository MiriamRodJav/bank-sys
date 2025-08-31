package com.xyz.bank.account_ms.repository;

import com.xyz.bank.account_ms.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    List<AccountEntity> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndActiveTrue(Long customerId);

}