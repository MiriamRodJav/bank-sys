package com.xyz.bank.account_ms.service.impl;

import com.xyz.bank.account_ms.entity.AccountEntity;
import com.xyz.bank.account_ms.entity.AccountType;
import com.xyz.bank.account_ms.mapper.AccountMapper;
import com.xyz.bank.account_ms.model.AccountModel;
import com.xyz.bank.account_ms.repository.AccountRepository;
import com.xyz.bank.account_ms.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public AccountModel createAccount(AccountModel account) {
        account.setAccountNumber(UUID.randomUUID().toString());
        if (account.getBalance() < 0) {
            throw new IllegalArgumentException("Initial balance must be greater or equal to 0");
        }
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity saved = accountRepository.save(entity);
        return accountMapper.toModel(saved);
    }

    @Override
    public List<AccountModel> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public AccountModel getAccountById(Long id) {
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        return accountMapper.toModel(entity);
    }

    @Override
    public AccountModel deposit(Long id, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        entity.setBalance(entity.getBalance() + amount);
        return accountMapper.toModel(accountRepository.save(entity));
    }

    @Override
    public AccountModel withdraw(Long id, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be positive");
        }
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        double newBalance = entity.getBalance() - amount;

        if (entity.getAccountType() == AccountType.SAVINGS && newBalance < 0) {
            throw new IllegalArgumentException("Insufficient funds in savings account");
        }
        if (entity.getAccountType() == AccountType.CURRENT && newBalance < -500) {
            throw new IllegalArgumentException("Overdraft limit exceeded in current account");
        }

        entity.setBalance(newBalance);
        return accountMapper.toModel(accountRepository.save(entity));
    }

    @Override
    public void deleteAccount(Long id) {
        AccountEntity entity = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        accountRepository.delete(entity);
        log.info("Deleted account id={}", id);
    }

    @Override
    public List<AccountModel> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toModel)
                .collect(Collectors.toList());
    }

}
