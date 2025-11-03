package com.store;

/**
 * Excepción personalizada para validación de productos
 */
public class InvalidProductException extends Exception {
    
    public InvalidProductException(String message) {
        super(message);
    }
    
    public InvalidProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
