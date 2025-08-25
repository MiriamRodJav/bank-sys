package com.nttdata.bank.repository;

import com.nttdata.bank.model.BankAccountModel;



public interface BankAccountRepository {
    BankAccountModel save(BankAccountModel account);

    boolean updateBalance(String accountNumber, double newBalance);

}
