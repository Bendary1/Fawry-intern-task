package com.ecommerce.model;


public class NonExpirableProduct extends Product {
    

    public NonExpirableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean isExpired() {
        return false; // Non-expirable products never expire
    }
    
    @Override
    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }
} 