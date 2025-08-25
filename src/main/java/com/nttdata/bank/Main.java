package com.nttdata.bank;

import com.nttdata.bank.service.BankAccountService;


public class Main {
    public static void main(String[] args) {
        BankAccountService bankService = new BankAccountService();
        // Register clients
        System.out.println("=== Registering Clients ===");
        bankService.registerClient("12345679", "Doe", "lastname", "john.doe@email.com");

        // Open Bank Account
        System.out.println("=== Open Bank Account===");
//        bankService.openBankAccount("12345678", AccountType.CURRENT);

        // Deposit Money
        System.out.println("=== Deposit Money===");
//        bankService.depositMoney("ACC1756149405991-87654310", 10000);

        // Withdraw Money
        System.out.println("=== Withdraw Money===");
//        bankService.withdrawMoney("ACC1756149405991-87654310", 10);

        // Check Balance
        System.out.println("=== Check Balance===");
//        bankService.checkBalance("ACC1756149405991-87654310");
    }
}