package com.store;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GESTIÓN DE INVENTARIO - TIENDA SEGUNDA MANO ===\n");
        
        // Probar conexión
        System.out.println("1. Probando conexión a la base de datos...");
        if (!DatabaseConnection.testConnection()) {
            System.err.println("❌ Error: No se pudo conectar a la base de datos.");
            System.out.println("   Verifica que MySQL esté ejecutándose y que los datos de conexión sean correctos.");
            return;
        }
        System.out.println("✅ Conexión establecida correctamente.\n");
        
        ProductDAO productDAO = new ProductDAO();
        
        try {
            // 1. Mostrar inventario inicial
            System.out.println("2. Inventario inicial:");
            List<Product> initialProducts = productDAO.getAllProducts();
            System.out.println("✅ Total de productos en inventario: " + initialProducts.size());
            
            // 2. Mostrar productos por categoría
            System.out.println("\n3. Productos por categoría:");
            showProductsByCategory(initialProducts, "Muebles");
            showProductsByCategory(initialProducts, "Música");
            showProductsByCategory(initialProducts, "Electrónica");
            showProductsByCategory(initialProducts, "Ropa");
            showProductsByCategory(initialProducts, "Decoración");
            showProductsByCategory(initialProducts, "Libros");
            showProductsByCategory(initialProducts, "Hogar");
            showProductsByCategory(initialProducts, "Deportes");
            showProductsByCategory(initialProducts, "Juguetes");
            showProductsByCategory(initialProducts, "Joyería");
            
            // 3. Crear nuevos productos
            System.out.println("\n4. Añadiendo nuevos productos...");
            try {
                Product newProduct1 = new Product("Bicicleta vintage holandesa", 220.00, 2, "Deportes", "Bicicleta clásica con canasta, perfecto estado");
                Product newProduct2 = new Product("Máquina de coser Singer", 95.00, 1, "Hogar", "Máquina de coser antigua funcionando, pie de hierro");
                
                Product created1 = productDAO.addProduct(newProduct1);
                Product created2 = productDAO.addProduct(newProduct2);
                
                System.out.println("✅ Nuevos productos añadidos:");
                System.out.println("   - " + created1);
                System.out.println("   - " + created2);
            } catch (InvalidProductException e) {
                System.out.println("❌ Error de validación al crear producto: " + e.getMessage());
            }
            
            // 4. Probar validaciones con productos inválidos
            System.out.println("\n4.1. Probando el sistema de validación...");
            testValidations(productDAO);
            
            // 5. Actualizar stock (simular venta)
            System.out.println("\n5. Simulando venta de productos...");
            updateProductStock(productDAO, 1, -1); // Se vende una mesa
            updateProductStock(productDAO, 5, -2); // Se venden 2 vinilos
            
            // 6. Buscar productos específicos
            System.out.println("\n6. Buscando productos específicos...");
            searchAndShowProduct(productDAO, 3); // Escritorio
            searchAndShowProduct(productDAO, 15); // Guitarra
            
            // 7. Mostrar productos con bajo stock
            System.out.println("\n7. Productos con stock bajo (< 3 unidades):");
            showLowStockProducts(productDAO.getAllProducts());
            
            // 8. Inventario final
            System.out.println("\n8. Inventario final completo:");
            List<Product> finalProducts = productDAO.getAllProducts();
            System.out.println("✅ Total de productos en inventario: " + finalProducts.size());
            
            // Resumen por categorías
            showInventorySummary(finalProducts);
            
            System.out.println("\n=== GESTIÓN DE INVENTARIO COMPLETADA ===");
            
        } catch (SQLException e) {
            System.err.println("❌ Error de base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método para probar las validaciones del sistema
     */
    private static void testValidations(ProductDAO dao) {
        System.out.println("   Probando validaciones:");
        
        // Test 1: Precio negativo
        System.out.println("\n   Test 1: Intentar crear producto con precio negativo");
        try {
            Product invalidProduct = new Product("Producto inválido", -50.00, 10, "Test", "Precio negativo");
            dao.addProduct(invalidProduct);
            System.err.println("   ❌ Error: No se detectó el precio negativo");
        } catch (InvalidProductException e) {
            System.out.println("   ✅ Validación correcta: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("   ❌ Error de BD: " + e.getMessage());
        }
        
        // Test 2: Stock negativo
        System.out.println("\n   Test 2: Intentar crear producto con stock negativo");
        try {
            Product invalidProduct = new Product("Producto inválido", 50.00, -5, "Test", "Stock negativo");
            dao.addProduct(invalidProduct);
            System.out.println("   ❌ Error: No se detectó el stock negativo");
        } catch (InvalidProductException e) {
            System.out.println("   ✅ Validación correcta: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("   ❌ Error de BD: " + e.getMessage());
        }
        
        // Test 3: Nombre vacío
        System.out.println("\n   Test 3: Intentar crear producto con nombre vacío");
        try {
            Product invalidProduct = new Product("", 50.00, 10, "Test", "Sin nombre");
            dao.addProduct(invalidProduct);
            System.out.println("   ❌ Error: No se detectó el nombre vacío");
        } catch (InvalidProductException e) {
            System.out.println("   ✅ Validación correcta: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("   ❌ Error de BD: " + e.getMessage());
        }
        
        // Test 4: Reducción de stock que resulta en negativo
        System.out.println("\n   Test 4: Intentar reducir stock más allá de cero");
        try {
            Product product = dao.getProductById(1);
            if (product != null) {
                int currentStock = product.getStock();
                System.out.println("   Stock actual: " + currentStock);
                product.reduceStock(currentStock + 5); // Intentar reducir más del stock disponible
                System.err.println("   ❌ Error: No se detectó la reducción inválida de stock");
            }
        } catch (InvalidProductException e) {
            System.out.println("   ✅ Validación correcta: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("   ❌ Error de BD: " + e.getMessage());
        }
        
        System.out.println("\n   ✅ Todas las validaciones funcionan correctamente");
    }
    
    // Métodos auxiliares para organizar la información
    private static void showProductsByCategory(List<Product> products, String category) {
        System.out.println("   " + category + ":");
        products.stream()
                .filter(p -> p.getCategory().equals(category))
                .forEach(p -> System.out.println("     - " + p.getName() + " (" + p.getStock() + " unidades)"));
    }
    
    private static void updateProductStock(ProductDAO dao, int productId, int stockChange) {
        try {
            Product product = dao.getProductById(productId);
            if (product != null) {
                int newStock = product.getStock() + stockChange;
                
                // Usar el método seguro de modificación de stock
                if (stockChange < 0) {
                    product.reduceStock(Math.abs(stockChange));
                } else {
                    product.addStock(stockChange);
                }
                
                dao.updateProduct(product);
                System.out.println("   ✅ " + product.getName() + " - Stock actualizado: " + product.getStock());
            }
        } catch (InvalidProductException e) {
            System.err.println("   ❌ Error de validación: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("   ❌ Error de base de datos: " + e.getMessage());
        }
    }
    
    private static void searchAndShowProduct(ProductDAO dao, int productId) throws SQLException {
        Product product = dao.getProductById(productId);
        if (product != null) {
            System.out.println("   ✅ Encontrado: " + product.getName() + 
                             " - Precio: " + product.getPrice() + "€ - Stock: " + product.getStock());
        } else {
            System.out.println("   ❌ Producto con ID " + productId + " no encontrado");
        }
    }
    
    private static void showLowStockProducts(List<Product> products) {
        products.stream()
                .filter(p -> p.getStock() < 3)
                .forEach(p -> System.out.println("   ⚠️  " + p.getName() + " - Solo " + p.getStock() + " unidades"));
    }
    
    private static void showInventorySummary(List<Product> products) {
        System.out.println("\n📊 Resumen del inventario:");
        products.stream()
                .collect(java.util.stream.Collectors.groupingBy(Product::getCategory, 
                        java.util.stream.Collectors.counting()))
                .forEach((category, count) -> 
                    System.out.println("   📁 " + category + ": " + count + " productos"));
        
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
        System.out.println("   💰 Valor total del inventario: " + String.format("%.2f", totalValue) + "€");
    }
}