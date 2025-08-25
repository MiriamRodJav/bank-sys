package com.nttdata.bank.repository;

import com.nttdata.bank.model.BankAccountModel;

import java.util.Optional;


public interface BankAccountRepository {
    BankAccountModel save(BankAccountModel account);

    boolean updateBalance(String accountNumber, double newBalance);

    Optional<BankAccountModel> findByAccountNumber(String accountNumber);

}
