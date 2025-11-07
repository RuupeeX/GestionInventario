package com.store;

/**
 * Clase para validar los datos de un producto
 */
public class ProductValidator {
    
    private static final double MIN_PRICE = 0.0;
    private static final int MIN_STOCK = 0;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    
    /**
     * Valida todos los campos de un producto
     * @param product Producto a validar
     * @throws InvalidProductException si algún campo no es válido
     */
    public static void validate(Product product) throws InvalidProductException {
        validateName(product.getName());
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateCategory(product.getCategory());
        validateDescription(product.getDescription());
    }
    
    /**
     * Valida el nombre del producto
     */
    public static void validateName(String name) throws InvalidProductException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductException("El nombre del producto no puede estar vacío");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidProductException(
                String.format("El nombre del producto no puede exceder %d caracteres", MAX_NAME_LENGTH)
            );
        }
    }
    
    /**
     * Valida el precio del producto
     */
    public static void validatePrice(double price) throws InvalidProductException {
        if (price < MIN_PRICE) {
            throw new InvalidProductException(
                String.format("El precio no puede ser negativo. Valor recibido: %.2f", price)
            );
        }
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            throw new InvalidProductException("El precio debe ser un número válido");
        }
    }
    
    /**
     * Valida el stock del producto
     */
    public static void validateStock(int stock) throws InvalidProductException {
        if (stock < MIN_STOCK) {
            throw new InvalidProductException(
                String.format("El stock no puede ser negativo. Valor recibido: %d", stock)
            );
        }
    }
    
    /**
     * Valida la categoría del producto
     */
    public static void validateCategory(String category) throws InvalidProductException {
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidProductException("La categoría del producto no puede estar vacía");
        }
    }
    
    /**
     * Valida la descripción del producto
     */
    public static void validateDescription(String description) throws InvalidProductException {
    	if (description == null || description.trim().isEmpty()) {
    		throw new InvalidProductException("La descripción no puede estar vacía");
    	}

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidProductException(
                String.format("La descripción no puede exceder %d caracteres", MAX_DESCRIPTION_LENGTH)
            );
        }
    }
    
    /**
     * Valida una actualización de stock
     */
    public static void validateStockUpdate(int currentStock, int quantity) throws InvalidProductException {
        int newStock = currentStock + quantity;
        if (newStock < MIN_STOCK) {
            throw new InvalidProductException(
                String.format("La operación resultaría en stock negativo. Stock actual: %d, Cantidad: %d", 
                    currentStock, quantity)
            );
        }
    }
}
