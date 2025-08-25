package com.nttdata.bank.repository;

import com.nttdata.bank.model.ClientModel;
import com.nttdata.bank.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    public ClientModel save(ClientModel client) throws SQLException {
        String sql = "INSERT INTO Client(firstName, lastName, dni, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getFirstName());
            stmt.setString(2, client.getLastName());
            stmt.setString(3, client.getDni());
            stmt.setString(4, client.getEmail());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                client.setId(rs.getLong(1));
            }
        }
        return client;
    }

    public ClientModel findByDni(String dni) throws SQLException {
        String sql = "SELECT * FROM Client WHERE dni = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new ClientModel(
                        rs.getLong("clientId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("dni"),
                        rs.getString("email")
                );
            }
        }
        return null;
    }

    public List<ClientModel> findAll() throws SQLException {
        List<ClientModel> clients = new ArrayList<>();
        String sql = "SELECT * FROM Client";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(new ClientModel(
                        rs.getLong("clientId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("dni"),
                        rs.getString("email")
                ));
            }
        }
        return clients;
    }
}
