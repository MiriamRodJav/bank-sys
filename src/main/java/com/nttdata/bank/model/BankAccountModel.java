package com.nttdata.bank.model;

import lombok.*;

@Data
public class BankAccountModel {

    private String accountNumber;
    private double balance;
    private AccountType accountType;
    private String clientDni;
    public BankAccountModel(String accountNumber, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = 0.0;
    }
    public BankAccountModel() {

    }
    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.balance += amount;
        return true;
    }
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            return false;
        }

        if (!validateWithdrawal(amount)) {
            return false;
        }

        this.balance -= amount;
        return true;
    }

    public boolean validateWithdrawal(double amount) {
        double potentialBalance = this.balance - amount;

        if (accountType == AccountType.SAVINGS) {
            return potentialBalance >= 0;
        } else if (accountType == AccountType.CURRENT) {
            return potentialBalance >= -500.0;
        }

        return false;
    }
}
