package com.ecommerce.model;

import com.ecommerce.interfaces.Shippable;


public class ShippableNonExpirableProduct extends NonExpirableProduct implements Shippable {
    private double weight;
    

    public ShippableNonExpirableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public double getWeight() { 
        return weight; 
    }
} 