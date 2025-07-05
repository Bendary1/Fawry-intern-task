package com.ecommerce.model;

import java.util.ArrayList;
import java.util.List;
import com.ecommerce.interfaces.Shippable;


public class Cart {
    private List<CartItem> items;
    

    public Cart() {
        this.items = new ArrayList<>();
    }
    

    public void add(Product product, int quantity) throws Exception {
        if (!product.isAvailable(quantity)) {
            if (product.isExpired()) {
                throw new Exception("Product " + product.getName() + " has expired");
            } else {
                throw new Exception("Product " + product.getName() + " is out of stock. Available: " + product.getQuantity());
            }
        }
        
        items.add(new CartItem(product, quantity));
        product.setQuantity(product.getQuantity() - quantity);
    }
    

    public List<CartItem> getItems() { 
        return items; 
    }
    

    public boolean isEmpty() { 
        return items.isEmpty(); 
    }
    

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
    

    public List<Shippable> getShippableItems() {
        List<Shippable> shippableItems = new ArrayList<>();
        for (CartItem item : items) {
            if (item.getProduct() instanceof Shippable) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    shippableItems.add((Shippable) item.getProduct());
                }
            }
        }
        return shippableItems;
    }
} 