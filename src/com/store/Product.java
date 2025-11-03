package com.store;

public class Product {
    private int id;
    private String name;
    private double price;
    private int stock;
    private String category;
    private String description;

    // Constructor sin id (para nuevos productos)
    public Product(String name, double price, int stock, String category, String description) 
            throws InvalidProductException {
        setName(name);
        setPrice(price);
        setStock(stock);
        setCategory(category);
        setDescription(description);
    }

    // Constructor completo (para productos existentes)
    public Product(int id, String name, double price, int stock, String category, String description) 
            throws InvalidProductException {
        this.id = id;
        setName(name);
        setPrice(price);
        setStock(stock);
        setCategory(category);
        setDescription(description);
    }

    // Getters y Setters con validación
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidProductException {
        ProductValidator.validateName(name);
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) throws InvalidProductException {
        ProductValidator.validatePrice(price);
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) throws InvalidProductException {
        ProductValidator.validateStock(stock);
        this.stock = stock;
    }
    
    /**
     * Añade stock de manera segura
     */
    public void addStock(int quantity) throws InvalidProductException {
        ProductValidator.validateStockUpdate(this.stock, quantity);
        this.stock += quantity;
    }
    
    /**
     * Reduce stock de manera segura
     */
    public void reduceStock(int quantity) throws InvalidProductException {
        ProductValidator.validateStockUpdate(this.stock, -quantity);
        this.stock -= quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) throws InvalidProductException {
        ProductValidator.validateCategory(category);
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws InvalidProductException {
        ProductValidator.validateDescription(description);
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%.2f, stock=%d, category='%s', description='%s'}",
                id, name, price, stock, category, description);
    }
}