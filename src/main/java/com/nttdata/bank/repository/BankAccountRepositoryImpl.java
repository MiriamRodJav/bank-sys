package com.nttdata.bank.repository;

import com.nttdata.bank.model.AccountType;
import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.util.DatabaseConnection;

import java.sql.*;


public class BankAccountRepositoryImpl implements BankAccountRepository{
    @Override
    public BankAccountModel save(BankAccountModel account) {
        String sql = "INSERT INTO bank_accounts (account_number, balance, account_type, client_id) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getAccountType().toString());
            stmt.setString(4, account.getClientDni());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return account;
            }
        } catch (SQLException e) {
            System.err.println("Error saving bank account: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return null;
    }



    @Override
    public boolean updateBalance(String accountNumber, double newBalance) {
        String sql = "UPDATE bank_accounts SET balance = ? WHERE account_number = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return false;
    }


}
