package com.xyz.bank.account_ms.service;

import com.xyz.bank.account_ms.model.AccountModel;

import java.util.List;

public interface AccountService {

    AccountModel createAccount(AccountModel account);

    List<AccountModel> getAllAccounts();

    AccountModel getAccountById(Long id);

    AccountModel deposit(Long id, Double amount);

    AccountModel withdraw(Long id, Double amount);

    void deleteAccount(Long id);

    List<AccountModel> getAccountsByCustomerId(Long customerId);

    boolean existsActiveAccountByCustomerId(Long customerId);

}
