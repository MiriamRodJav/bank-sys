package com.nttdata.bank;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.service.BankAccountService;
import com.nttdata.bank.service.ClientService;

public class Main {
    public static void main(String[] args) {
        try {

            // Crear servicios
            ClientService clientService = new ClientService();
            BankAccountService bankAccountService = new BankAccountService();

            // 1. Registrar un cliente
            ClientModel client = new ClientModel();
            client.setFirstName("Lourdes");
            client.setLastName("Cabanillas");
            client.setDni("12345678");
            client.setEmail("lourdes@mail.com");

            ClientModel savedClient = clientService.registerClient(client);
            System.out.println("Cliente registrado: " + savedClient.getFirstName() + " " + savedClient.getLastName());

            // 2. Abrir una cuenta bancaria para el cliente
            BankAccountModel account = new BankAccountModel();
            account.setClientId(savedClient.getClientId());
            account.setAccountType(BankAccountModel.AccountType.SAVINGS);
            account.setBalance(1000.0);

            BankAccountModel savedAccount = bankAccountService.openAccount(account);
            System.out.println("Cuenta creada con Nro: " + savedAccount.getAccountNumber() + " y saldo inicial: " + savedAccount.getBalance());

            // 3. Hacer un depósito
            BankAccountModel afterDeposit = bankAccountService.deposit(savedAccount.getAccountNumber(), 500.0);
            System.out.println("Saldo después de depósito: " + afterDeposit.getBalance());

            // 4. Hacer un retiro
            BankAccountModel afterWithdraw = bankAccountService.withdraw(savedAccount.getAccountNumber(), 200.0);
            System.out.println("Saldo después de retiro: " + afterWithdraw.getBalance());

            // 5. Consultar saldo
            double balance = bankAccountService.checkBalance(savedAccount.getAccountNumber());
            System.out.println("Saldo final: " + balance);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
