package com.nttdata.bank.repository;

import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.util.DatabaseConnection;

import java.sql.*;

import java.util.Optional;

public class ClientRepositoryImpl implements ClientRepository{
    @Override
    public ClientModel save(ClientModel client) {
        String sql = "INSERT INTO clients (dni, first_name, last_name,  email) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try  {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, client.getDni());
            stmt.setString(2, client.getFirstName());
            stmt.setString(3, client.getLastName());
            stmt.setString(4, client.getEmail());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return client;
            }
        } catch (SQLException e) {
            System.err.println("Error saving client: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return null;
    }

    @Override
    public Optional<ClientModel> findByDni(String dni) {
        String sql = "SELECT * FROM clients WHERE dni = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding client by DNI: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        return Optional.empty();
    }



    @Override
    public boolean delete(String dni) {
        String sql = "DELETE FROM clients WHERE dni = ?";
        Connection conn = null;
        try  {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dni);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
        }finally {
            DatabaseConnection.closeConnection(conn);
        }
        return false;
    }

    @Override
    public boolean existsByDni(String dni) {
        String sql = "SELECT COUNT(*) FROM clients WHERE dni = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking client existence: " + e.getMessage());
        }finally {
            DatabaseConnection.closeConnection(conn);
        }
        return false;
    }

    private ClientModel mapResultSetToClient(ResultSet rs) throws SQLException {
        ClientModel client = new ClientModel(
                rs.getString("dni"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email")
        );
        return client;
    }
}
