package com.nttdata.bank.service;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.BankAccountRepository;
import com.nttdata.bank.repository.ClientRepository;

import java.util.Random;
import java.util.UUID;

/**
 * Service class for managing bank account operations.
 */
public class BankAccountService {

    private final BankAccountRepository repositoryBankAccount = new BankAccountRepository();
    private final ClientRepository repositoryClient = new ClientRepository();

    /**
     * Creates a new bank account after validating client existence and account rules.
     *
     * @param bankAccount the account to create
     * @return the saved bank account
     * @throws Exception if required fields are missing,
     *                   if client does not exist,
     *                   if account type is invalid,
     *                   if a savings account has a negative balance,
     *                   or if a checking account exceeds the overdraft limit
     */
    public BankAccountModel openAccount(BankAccountModel bankAccount) throws Exception {

        if (bankAccount.getClientId() == null || bankAccount.getAccountType() == null) {
            throw new Exception("All fields are required");
        }

        ClientModel client = repositoryClient.findById(bankAccount.getClientId());
        if (client == null){
            throw new Exception("Client not found");
        }

        String accountNumber;
        do {
            Random random = new Random();
            long number = 1000000000L + (long)(random.nextDouble() * 9999999999L);
            accountNumber =  String.valueOf(number);
        } while (repositoryBankAccount.findByAccountNumber(accountNumber) != null);

        bankAccount.setAccountNumber(accountNumber);

        if (bankAccount.getAccountType() != BankAccountModel.AccountType.SAVINGS
                && bankAccount.getAccountType() != BankAccountModel.AccountType.CHECKING) {
            throw new Exception("Account type must be SAVINGS or CHECKING");
        }

        if (bankAccount.getAccountType() == BankAccountModel.AccountType.SAVINGS && bankAccount.getBalance() < 0) {
            throw new Exception("Savings accounts cannot have a negative balance");
        }

        if (bankAccount.getAccountType() == BankAccountModel.AccountType.CHECKING && bankAccount.getBalance() < -500.00) {
            throw new Exception("Checking accounts cannot have a balance below -500.00");
        }

        return repositoryBankAccount.save(bankAccount);

    }

    /**
     * Deposits an amount into a bank account identified by account number.
     *
     * @param accountNumber the account number where the deposit will be made
     * @param amount the amount to deposit
     * @return the updated bank account
     * @throws Exception if the account does not exist or amount is invalid
     */
    public BankAccountModel deposit(String accountNumber, double amount) throws Exception {

        if (amount <= 0) {
            throw new Exception("Deposit amount must be greater than zero");
        }

        BankAccountModel account = repositoryBankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new Exception("Account not found");
        }

        double newBalance = account.getBalance() + amount;
        repositoryBankAccount.updateBalance(account.getAccountId(), newBalance);

        account.setBalance(newBalance);
        return account;
    }

    /**
     * Withdraws money from a bank account
     *
     * @param accountNumber the account number where the withdrawal will be made
     * @param amount the amount to withdraw
     * @return the updated bank account
     * @throws Exception if account does not exist, amount is invalid, or rules are violated
     */
    public BankAccountModel withdraw(String accountNumber, double amount) throws Exception {

        if (amount <= 0) {
            throw new Exception("Withdrawal amount must be greater than zero");
        }

        BankAccountModel account = repositoryBankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new Exception("Account not found");
        }

        double newBalance = account.getBalance() - amount;

        if (account.getAccountType() == BankAccountModel.AccountType.SAVINGS && newBalance < 0) {
            throw new Exception("Savings accounts cannot have a negative balance");
        }

        if (account.getAccountType() == BankAccountModel.AccountType.CHECKING && newBalance < -500.00) {
            throw new Exception("Checking accounts cannot go below -500.00");
        }

        repositoryBankAccount.updateBalance(account.getAccountId(), newBalance);

        account.setBalance(newBalance);
        return account;
    }

    /**
     * Retrieves the current balance of a bank account by its account number.
     *
     * @param accountNumber the account number to consult
     * @return the balance of the account
     * @throws Exception if the account does not exist
     */
    public double checkBalance(String accountNumber) throws Exception {
        BankAccountModel account = repositoryBankAccount.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new Exception("Account not found");
        }
        return account.getBalance();
    }

}
