import java.time.LocalDate;
import java.util.List;

import com.ecommerce.model.*;
import com.ecommerce.interfaces.*;
import com.ecommerce.services.*;


public class Main {
    

    public static void checkout(Customer customer, Cart cart) throws Exception {
        // Check if cart is empty
        if (cart.isEmpty()) {
            throw new Exception("Cart is empty");
        }
        
        // Calculate costs
        double subtotal = cart.getSubtotal();
        List<Shippable> shippableItems = cart.getShippableItems();
        double shippingFee = ShippingService.calculateShippingFee(shippableItems);
        double totalAmount = subtotal + shippingFee;
        
        // Check if customer can afford
        if (!customer.canAfford(totalAmount)) {
            throw new Exception("Insufficient balance. Required: " + totalAmount + ", Available: " + customer.getBalance());
        }
        
        // Process shipping
        ShippingService.printShipmentNotice(shippableItems);
        
        // Print checkout receipt
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.println(item.getQuantity() + "x " + item.getProduct().getName() + " " + (int)item.getTotalPrice());
        }
        System.out.println("-----------------------");
        System.out.println("Subtotal " + (int)subtotal);
        System.out.println("Shipping " + (int)shippingFee);
        System.out.println("Amount " + (int)totalAmount);
        
        // Process payment
        customer.deductBalance(totalAmount);
        System.out.println("Customer balance after payment: " + customer.getBalance());
    }
    

    public static void main(String[] args) {
        try {
            // Create products
            ShippableExpirableProduct cheese = new ShippableExpirableProduct("Cheese", 100, 5, LocalDate.now().plusDays(7), 0.2);
            ShippableExpirableProduct biscuits = new ShippableExpirableProduct("Biscuits", 150, 3, LocalDate.now().plusDays(30), 0.7);
            ShippableNonExpirableProduct tv = new ShippableNonExpirableProduct("TV", 500, 2, 15.0);
            NonExpirableProduct scratchCard = new NonExpirableProduct("Mobile Scratch Card", 50, 10);
            
            // Create customer
            Customer customer = new Customer("John Doe", 1000);
            
            // Create cart and add items
            Cart cart = new Cart();
            
            System.out.println("=== SUCCESSFUL CHECKOUT EXAMPLE ===");
            cart.add(cheese, 2);
            cart.add(biscuits, 1);
            cart.add(scratchCard, 1);
            
            checkout(customer, cart);
            
            System.out.println("\n=== TESTING ERROR CASES ===");
            
            // Test case 1: Empty cart
            try {
                Cart emptyCart = new Cart();
                checkout(customer, emptyCart);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            // Test case 2: Insufficient balance
            try {
                Cart expensiveCart = new Cart();
                expensiveCart.add(tv, 2); // 2 TVs = 1000, but we also have shipping
                checkout(customer, expensiveCart);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            // Test case 3: Out of stock
            try {
                Cart cart2 = new Cart();
                cart2.add(cheese, 10); // Only 3 cheese left after previous purchase
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            // Test case 4: Expired product
            try {
                ShippableExpirableProduct expiredCheese = new ShippableExpirableProduct("Expired Cheese", 100, 5, LocalDate.now().minusDays(1), 0.2);
                Cart cart3 = new Cart();
                cart3.add(expiredCheese, 1);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            // Test case 5: Non-shippable items only
            System.out.println("\n=== NON-SHIPPABLE ITEMS ONLY ===");
            Cart digitalCart = new Cart();
            digitalCart.add(scratchCard, 2);
            checkout(customer, digitalCart);
            
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }
}
