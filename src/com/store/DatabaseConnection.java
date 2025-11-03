package com.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/store_db";
    private static final String USER = "root"; // Cambiar por tu usuario
    private static final String PASSWORD = "Movistar04"; // Cambiar por tu contraseña

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver no encontrado", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método para probar la conexión
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return true;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            return false;
        }
    }
}
