import java.time.LocalDate;
import java.util.*;

// Base Product class
abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;
    
    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public abstract boolean isExpired();
    public abstract boolean isAvailable(int requestedQuantity);
}

// Expirable Product class
class ExpirableProduct extends Product {
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
    
    public LocalDate getExpirationDate() { return expirationDate; }
}

// Non-Expirable Product class
class NonExpirableProduct extends Product {
    public NonExpirableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean isExpired() {
        return false;
    }
    
    @Override
    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }
}

// Shippable interface
interface Shippable {
    String getName();
    double getWeight();
}

// Shippable Expirable Product
class ShippableExpirableProduct extends ExpirableProduct implements Shippable {
    private double weight;
    
    public ShippableExpirableProduct(String name, double price, int quantity, LocalDate expirationDate, double weight) {
        super(name, price, quantity, expirationDate);
        this.weight = weight;
    }
    
    @Override
    public double getWeight() { return weight; }
}

// Shippable Non-Expirable Product
class ShippableNonExpirableProduct extends NonExpirableProduct implements Shippable {
    private double weight;
    
    public ShippableNonExpirableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public double getWeight() { return weight; }
}

// Customer class
class Customer {
    private String name;
    private double balance;
    
    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }
    
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

// Cart Item class
class CartItem {
    private Product product;
    private int quantity;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return product.getPrice() * quantity; }
}

// Cart class
class Cart {
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
    
    public List<CartItem> getItems() { return items; }
    
    public boolean isEmpty() { return items.isEmpty(); }
    
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

// Shipping Service class
class ShippingService {
    private static final double SHIPPING_RATE_PER_KG = 10.0;
    
    public static double calculateShippingFee(List<Shippable> shippableItems) {
        if (shippableItems.isEmpty()) {
            return 0.0;
        }
        
        double totalWeight = shippableItems.stream().mapToDouble(Shippable::getWeight).sum();
        return Math.ceil(totalWeight) * SHIPPING_RATE_PER_KG;
    }
    
    public static void printShipmentNotice(List<Shippable> shippableItems) {
        if (shippableItems.isEmpty()) {
            return;
        }
        
        System.out.println("** Shipment notice **");
        
        // Group items by name and calculate total weight
        Map<String, Integer> itemCounts = new HashMap<>();
        Map<String, Double> itemWeights = new HashMap<>();
        
        for (Shippable item : shippableItems) {
            String name = item.getName();
            itemCounts.put(name, itemCounts.getOrDefault(name, 0) + 1);
            itemWeights.put(name, item.getWeight());
        }
        
        double totalWeight = 0;
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();
            double singleWeight = itemWeights.get(name);
            double totalItemWeight = singleWeight * count;
            totalWeight += totalItemWeight;
            
            System.out.println(count + "x " + name + " " + (int)(totalItemWeight * 1000) + "g");
        }
        
        System.out.println("Total package weight " + String.format("%.1f", totalWeight) + "kg");
    }
}

// Main class with checkout functionality
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
        System.out.println("----------------------");
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