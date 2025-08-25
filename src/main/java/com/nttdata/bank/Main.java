package com.nttdata.bank;

import com.nttdata.bank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            System.out.println("Intentando conectar a la base de datos...");
            connection = DatabaseConnection.getConnection();
            if (connection != null) {
                System.out.println("¡Conexión exitosa!");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(connection);
            System.out.println("Conexión cerrada.");
        }
    }
}