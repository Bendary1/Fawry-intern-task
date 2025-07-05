package com.ecommerce.model;


public class Customer {
    private String name;
    private double balance;
    

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    

    public boolean canAfford(double amount) {
        return balance >= amount;
    }
    

    public void deductBalance(double amount) {
        if (canAfford(amount)) {
            balance -= amount;
        }
    }
} 