package com.ecommerce.model;

import java.time.LocalDate;


public class ExpirableProduct extends Product {
    private LocalDate expirationDate;
    

    public ExpirableProduct(String name, double price, int quantity, LocalDate expirationDate) {
        super(name, price, quantity);
        this.expirationDate = expirationDate;
    }
    
    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }
    
    @Override
    public boolean isAvailable(int requestedQuantity) {
        return !isExpired() && quantity >= requestedQuantity;
    }
    

    public LocalDate getExpirationDate() { 
        return expirationDate; 
    }
} 