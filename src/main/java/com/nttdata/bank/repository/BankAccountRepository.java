package com.nttdata.bank.repository;

import com.nttdata.bank.model.BankAccountModel;
import com.nttdata.bank.util.DatabaseConnection;
import java.sql.*;

/**
 * Repository class for managing persistence operations of BankAccountModel.
 * Provides methods to save, find and update bank accounts in the database.
 */
public class BankAccountRepository {

    /**
     * Saves a new bank account into the database.
     *
     * @param account the BankAccountModel to be saved
     * @return the saved account with the generated accountId
     * @throws SQLException if any SQL error occurs during the insert
     */
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

    /**
     * Finds a bank account by its account number.
     *
     * @param accountNumber the unique account number to search
     * @return the BankAccountModel if found, or null if not found
     * @throws SQLException if any SQL error occurs during the query
     */
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

    /**
     * Updates the balance of a specific account.
     *
     * @param accountId  the unique identifier of the account to update
     * @param newBalance the new balance value to be set
     * @throws SQLException if any SQL error occurs during the update
     */
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
