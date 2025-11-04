package com.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    /**
     * Añade un producto a la base de datos con validación
     */
    public Product addProduct(Product product) throws SQLException, InvalidProductException {
        // Validar el producto antes de insertarlo
        ProductValidator.validate(product);
        
        String sql = "INSERT INTO products (name, price, stock, category, description) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStock());
            stmt.setString(4, product.getCategory());
            stmt.setString(5, product.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La creación del producto falló, no se insertaron filas.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                    return product;
                } else {
                    throw new SQLException("La creación del producto falló, no se obtuvo el ID.");
                }
            }
        }
    }

    /**
     * Obtiene un producto por su ID
     */
    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    try {
                        return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("category"),
                            rs.getString("description")
                        );
                    } catch (InvalidProductException e) {
                        // Si los datos en la BD son inválidos, loguearlo
                        throw new SQLException("Datos inválidos en la base de datos para el producto ID: " + id, e);
                    }
                } else {
                    return null; // Producto no encontrado
                }
            }
        }
    }

    /**
     * Obtiene todos los productos
     */
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                try {
                    Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        rs.getString("description")
                    );
                    products.add(product);
                } catch (InvalidProductException e) {
                    // Registrar el error pero continuar con los demás productos
                    System.err.println("Producto inválido ignorado (ID: " + rs.getInt("id") + "): " + e.getMessage());
                }
            }
        }
        
        return products;
    }

    /**
     * Actualiza un producto con validación
     */
    public boolean updateProduct(Product product) throws SQLException, InvalidProductException {
        // Validar el producto antes de actualizarlo
        ProductValidator.validate(product);
        
        String sql = "UPDATE products SET name = ?, price = ?, stock = ?, category = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStock());
            stmt.setString(4, product.getCategory());
            stmt.setString(5, product.getDescription());
            stmt.setInt(6, product.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Elimina un producto por su ID
     */
    public boolean deleteProduct(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Actualiza solo el stock de un producto con validación
     */
    public boolean updateStock(int productId, int newStock) throws SQLException, InvalidProductException {
        // Validar el nuevo stock
        ProductValidator.validateStock(newStock);
        
        String sql = "UPDATE products SET stock = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Añade cantidad al stock existente de manera segura
     */
    public boolean addToStock(int productId, int quantity) throws SQLException, InvalidProductException {
        Product product = getProductById(productId);
        
        if (product == null) {
            throw new SQLException("Producto no encontrado con ID: " + productId);
        }
        
        // Validar que la operación no resulte en stock negativo
        ProductValidator.validateStockUpdate(product.getStock(), quantity);
        
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}