package com.ecommerce.model;


public abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;
    

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    

    public abstract boolean isExpired();
    

    public abstract boolean isAvailable(int requestedQuantity);
} 