package com.nttdata.bank.service;

import com.nttdata.bank.model.AccountType;
import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.BankAccountRepository;
import com.nttdata.bank.repository.BankAccountRepositoryImpl;
import com.nttdata.bank.repository.ClientRepository;
import com.nttdata.bank.repository.ClientRepositoryImpl;

import java.util.Optional;

public class BankAccountService {

    private ClientRepository clientRepository;
    private BankAccountRepository bankAccountRepository;

    public BankAccountService() {
        this.clientRepository = new ClientRepositoryImpl();
        this.bankAccountRepository = new BankAccountRepositoryImpl();
    }

    /**
     * Registers a new client
     */
    public ClientModel registerClient(String dni, String firstName, String lastName,  String email) {
        // Check if DNI already exists
        if (clientRepository.existsByDni(dni)) {
            System.out.println("Client with DNI " + dni + " already exists.");
            return null;
        }
        // Validate mandatory fields
        if (dni == null || dni.trim().isEmpty()|| firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            System.out.println("All mandatory fields must be provided.");
            return null;
        }
        // Validate email format
        ClientModel tempClient = new ClientModel(dni, firstName, lastName,  email);

        if (!tempClient.validateEmail()) {
            System.out.println("Invalid email format: " + email);
            return null;
        }

        if (!tempClient.validateDni()) {
            System.out.println("Invalid DNI format: " + dni);
            return null;
        }

        ClientModel savedClient = clientRepository.save(tempClient);

        if (savedClient != null) {
            System.out.println("Client registered successfully: " + savedClient);
        } else {
            System.out.println("Failed to register client.");
        }

        return savedClient;
    }

    /**
     * Opens a new bank account for a client
     */
    public BankAccountModel openBankAccount(String clientDni, AccountType accountType) {
        // Find client
        Optional<ClientModel> clientOpt = clientRepository.findByDni(clientDni);
        if (clientOpt.isEmpty()) {
            System.out.println("Client with DNI " + clientDni + " not found.");
            return null;
        }

        ClientModel client = clientOpt.get();

        // Create account
        String accountNumber = generateAccountNumber(clientDni);
        BankAccountModel newAccount = new BankAccountModel(accountNumber, accountType);
        newAccount.setClientDni(client.getDni());
        // Save account to database
        BankAccountModel savedAccount = bankAccountRepository.save(newAccount);

        if (savedAccount != null) {
            System.out.println("Account opened successfully: " + savedAccount);
        } else {
            System.out.println("Failed to open account.");
        }

        return savedAccount;
    }

    /**
     * Deposits money into an account
     */
    public boolean depositMoney(String accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return false;
        }

        Optional<BankAccountModel> accountOpt = bankAccountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            System.out.println("Account " + accountNumber + " not found.");
            return false;
        }

        BankAccountModel account = accountOpt.get();
        double newBalance = account.getBalance() + amount;

        // Update balance in database
        boolean success = bankAccountRepository.updateBalance(accountNumber, newBalance);

        if (success) {
            System.out.println("Deposit of " + amount + " to account " + accountNumber + " successful.");
        } else {
            System.out.println("Deposit failed.");
        }

        return success;
    }

    /**
     * Withdraws money from an account
     */
    public boolean withdrawMoney(String accountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }

        Optional<BankAccountModel> accountOpt = bankAccountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            System.out.println("Account " + accountNumber + " not found.");
            return false;
        }

        BankAccountModel account = accountOpt.get();

        if (!account.validateWithdrawal(amount)) {
            System.out.println("Withdrawal failed. Insufficient funds or invalid amount.");
            return false;
        }

        double newBalance = account.getBalance() - amount;

        // Update balance in database
        boolean success = bankAccountRepository.updateBalance(accountNumber, newBalance);

        if (success) {
            System.out.println("Withdrawal of " + amount + " from account " + accountNumber + " successful.");
        } else {
            System.out.println("Withdrawal failed.");
        }

        return success;
    }

    /**
     * Checks the balance of an account
     */
    public Double checkBalance(String accountNumber) {
        Optional<BankAccountModel> accountOpt = bankAccountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            System.out.println("Account " + accountNumber + " not found.");
            return null;
        }

        double balance = accountOpt.get().getBalance();
        System.out.println("Account " + accountNumber + " balance: " + balance);
        return balance;
    }



    /**
     * Generates a unique account number
     */
    private String generateAccountNumber(String clientDni) {
        return "ACC" + System.currentTimeMillis() + "-" + clientDni;
    }

}
