package com.nttdata.bank.repository;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankAccountRepository {

    public BankAccountModel save(BankAccountModel account) throws SQLException {
        String sql = "INSERT INTO BankAccount(accountNumber, balance, accountType, clientId) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getAccountNumber());
            stmt.setDouble(2, account.getBalance());
            stmt.setString(3, account.getAccountType().name());
            stmt.setLong(4, account.getClientId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                account.setAccountId(rs.getLong(1));
            }
        }
        return account;
    }

    public BankAccountModel findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM BankAccount WHERE accountNumber = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new BankAccountModel(
                        rs.getLong("accountId"),
                        rs.getString("accountNumber"),
                        rs.getDouble("balance"),
                        BankAccountModel.AccountType.valueOf(rs.getString("accountType")),
                        rs.getLong("clientId")
                );
            }
        }
        return null;
    }

    public List<BankAccountModel> findByClientId(Long clientId) throws SQLException {
        List<BankAccountModel> accounts = new ArrayList<>();
        String sql = "SELECT * FROM BankAccount WHERE clientId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(new BankAccountModel(
                        rs.getLong("accountId"),
                        rs.getString("accountNumber"),
                        rs.getDouble("balance"),
                        BankAccountModel.AccountType.valueOf(rs.getString("accountType")),
                        rs.getLong("clientId")
                ));
            }
        }
        return accounts;
    }

    public void updateBalance(Long accountId, double newBalance) throws SQLException {
        String sql = "UPDATE BankAccount SET balance = ? WHERE accountId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setLong(2, accountId);
            stmt.executeUpdate();
        }
    }
}
