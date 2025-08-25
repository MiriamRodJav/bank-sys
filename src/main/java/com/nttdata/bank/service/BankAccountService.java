package com.nttdata.bank.service;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.repository.BankAccountRepository;
import com.nttdata.bank.repository.ClientRepository;

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
            accountNumber = UUID.randomUUID().toString().substring(0, 10);
        } while (repositoryBankAccount.findByAccountNumber(accountNumber) != null);

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

}
