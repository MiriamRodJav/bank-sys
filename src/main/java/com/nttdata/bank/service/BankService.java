package com.nttdata.bank.service;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.repository.BankAccountRepository;
import com.nttdata.bank.repository.ClientRepository;

public class BankAccountService {
    private ClientRepository clientRepository;
    private BankAccountRepository bankAccountRepository;

    public void deposit(BankAccountModel account, double amount) throws Exception {
        if (amount <= 0) throw new Exception("Deposit amount must be positive");
        double newBalance = account.getBalance() + amount;
        repository.updateBalance(account.getAccountId(), newBalance);
        account.setBalance(newBalance);
    }

    public void withdraw(BankAccountModel account, double amount) throws Exception {
        if (amount <= 0) throw new Exception("Withdraw amount must be positive");
        double newBalance = account.getBalance() - amount;

        if (account.getAccountType() == BankAccountModel.AccountType.SAVINGS && newBalance < 0) {
            throw new Exception("Savings accounts cannot go negative");
        }

        if (account.getAccountType() == BankAccountModel.AccountType.CHECKING && newBalance < -500) {
            throw new Exception("Checking accounts cannot exceed overdraft limit of -500");
        }

        repository.updateBalance(account.getAccountId(), newBalance);
        account.setBalance(newBalance);
    }

    public double checkBalance(BankAccountModel account) {
        return account.getBalance();
    }
}
