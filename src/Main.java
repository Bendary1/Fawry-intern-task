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
    public boolean isInStock(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }
}

// Expirable products
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
}

// Non-expirable products
class NonExpirableProduct extends Product {
    public NonExpirableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }
    
    @Override
    public boolean isExpired() {
        return false;
    }
}

// Interface for shippable items
interface Shippable {
    String getName();
    double getWeight();
}

// Shippable expirable product
class ShippableExpirableProduct extends ExpirableProduct implements Shippable {
    private double weight;
    
    public ShippableExpirableProduct(String name, double price, int quantity, LocalDate expirationDate, double weight) {
        super(name, price, quantity, expirationDate);
        this.weight = weight;
    }
    
    @Override
    public double getWeight() { return weight; }
}

// Shippable non-expirable product
class ShippableNonExpirableProduct extends NonExpirableProduct implements Shippable {
    private double weight;
    
    public ShippableNonExpirableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }
    
    @Override
    public double getWeight() { return weight; }
}

// Cart item to track quantity
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

// Shopping cart
class Cart {
    private List<CartItem> items = new ArrayList<>();
    
    public void add(Product product, int quantity) throws Exception {
        if (product.isExpired()) {
            throw new Exception("Cannot add expired product: " + product.getName());
        }
        if (!product.isInStock(quantity)) {
            throw new Exception("Insufficient stock for product: " + product.getName() + 
                              " (Available: " + product.getQuantity() + ", Requested: " + quantity + ")");
        }
        
        // Check if product already exists in cart
        for (CartItem item : items) {
            if (item.getProduct().equals(product)) {
                int newQuantity = item.getQuantity() + quantity;
                if (!product.isInStock(newQuantity)) {
                    throw new Exception("Insufficient stock for product: " + product.getName() + 
                                      " (Available: " + product.getQuantity() + ", Total requested: " + newQuantity + ")");
                }
                items.remove(item);
                items.add(new CartItem(product, newQuantity));
                return;
            }
        }
        
        items.add(new CartItem(product, quantity));
    }
    
    public List<CartItem> getItems() { return new ArrayList<>(items); }
    public boolean isEmpty() { return items.isEmpty(); }
    
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
    
    public void clear() { items.clear(); }
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
    
    public void deductBalance(double amount) throws Exception {
        if (balance < amount) {
            throw new Exception("Insufficient balance. Required: " + amount + ", Available: " + balance);
        }
        balance -= amount;
    }
}

// Shipping service
class ShippingService {
    private static final double SHIPPING_RATE_PER_KG = 10.0; // $10 per kg
    private static final double BASE_SHIPPING_FEE = 5.0;     // $5 base fee
    
    public double calculateShippingFee(List<Shippable> items) {
        if (items.isEmpty()) return 0;
        
        double totalWeight = items.stream().mapToDouble(Shippable::getWeight).sum();
        return BASE_SHIPPING_FEE + (totalWeight * SHIPPING_RATE_PER_KG);
    }
    
    public void processShipment(List<Shippable> items, Map<String, Integer> quantities) {
        if (items.isEmpty()) return;
        
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        
        for (Shippable item : items) {
            int qty = quantities.getOrDefault(item.getName(), 1);
            double itemWeight = item.getWeight() * qty;
            totalWeight += itemWeight;
            
            System.out.println(qty + "x " + item.getName() + " " + 
                             String.format("%.0f", itemWeight * 1000) + "g");
        }
        
        System.out.println("Total package weight " + String.format("%.1f", totalWeight) + "kg");
    }
}

// Main e-commerce system
public class Main {
    
    public static void checkout(Customer customer, Cart cart) {
        try {
            // Check if cart is empty
            if (cart.isEmpty()) {
                throw new Exception("Cart is empty");
            }
            
            // Validate all items before checkout
            List<Shippable> shippableItems = new ArrayList<>();
            Map<String, Integer> shippableQuantities = new HashMap<>();
            
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();
                
                // Check if product is expired
                if (product.isExpired()) {
                    throw new Exception("Product expired: " + product.getName());
                }
                
                // Check if product is in stock
                if (!product.isInStock(quantity)) {
                    throw new Exception("Product out of stock: " + product.getName() + 
                                      " (Available: " + product.getQuantity() + ", Requested: " + quantity + ")");
                }
                
                // Collect shippable items
                if (product instanceof Shippable) {
                    shippableItems.add((Shippable) product);
                    shippableQuantities.put(product.getName(), quantity);
                }
            }
            
            // Calculate costs
            double subtotal = cart.getSubtotal();
            ShippingService shippingService = new ShippingService();
            double shippingFee = shippingService.calculateShippingFee(shippableItems);
            double totalAmount = subtotal + shippingFee;
            
            // Check customer balance
            if (customer.getBalance() < totalAmount) {
                throw new Exception("Insufficient balance. Required: " + totalAmount + 
                                  ", Available: " + customer.getBalance());
            }
            
            // Process shipment
            shippingService.processShipment(shippableItems, shippableQuantities);
            
            // Update product quantities
            for (CartItem item : cart.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() - item.getQuantity());
            }
            
            // Process payment
            customer.deductBalance(totalAmount);
            
            // Print checkout receipt
            System.out.println("** Checkout receipt **");
            for (CartItem item : cart.getItems()) {
                System.out.println(item.getQuantity() + "x " + item.getProduct().getName() + 
                                 " " + String.format("%.0f", item.getTotalPrice()));
            }
            System.out.println("----------------------");
            System.out.println("Subtotal " + String.format("%.0f", subtotal));
            System.out.println("Shipping " + String.format("%.0f", shippingFee));
            System.out.println("Amount " + String.format("%.0f", totalAmount));
            System.out.println("Customer balance after payment: " + String.format("%.2f", customer.getBalance()));
            
            // Clear cart after successful checkout
            cart.clear();
            
        } catch (Exception e) {
            System.err.println("Checkout failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        try {
            // Create products
            Product cheese = new ShippableExpirableProduct("Cheese", 100, 10, 
                LocalDate.now().plusDays(5), 0.2); // 200g per unit
            Product biscuits = new ShippableExpirableProduct("Biscuits", 150, 5, 
                LocalDate.now().plusDays(30), 0.7); // 700g per unit
            Product tv = new ShippableNonExpirableProduct("TV", 500, 3, 5.0); // 5kg per unit
            Product mobile = new NonExpirableProduct("Mobile", 300, 8);
            Product scratchCard = new NonExpirableProduct("Scratch Card", 50, 20);
            
            // Create customer
            Customer customer = new Customer("John Doe", 1000.0);
            
            // Create cart and add items
            Cart cart = new Cart();
            
            System.out.println("=== Test Case 1: Successful Checkout ===");
            cart.add(cheese, 2);
            cart.add(biscuits, 1);
            cart.add(scratchCard, 1);
            
            System.out.println("Customer balance before: " + customer.getBalance());
            checkout(customer, cart);
            
            System.out.println("\n=== Test Case 2: Adding TV ===");
            cart.add(tv, 1);
            cart.add(mobile, 1);
            checkout(customer, cart);
            
            System.out.println("\n=== Test Case 3: Empty Cart Error ===");
            Cart emptyCart = new Cart();
            checkout(customer, emptyCart);
            
            System.out.println("\n=== Test Case 4: Insufficient Stock Error ===");
            try {
                cart.add(tv, 5); // Only 2 TVs left after previous purchase
            } catch (Exception e) {
                System.err.println("Add to cart failed: " + e.getMessage());
            }
            
            System.out.println("\n=== Test Case 5: Expired Product Error ===");
            Product expiredCheese = new ShippableExpirableProduct("Expired Cheese", 100, 5, 
                LocalDate.now().minusDays(1), 0.2);
            try {
                cart.add(expiredCheese, 1);
            } catch (Exception e) {
                System.err.println("Add to cart failed: " + e.getMessage());
            }
            
            System.out.println("\n=== Test Case 6: Insufficient Balance Error ===");
            Customer poorCustomer = new Customer("Poor Customer", 50.0);
            Cart expensiveCart = new Cart();
            expensiveCart.add(tv, 1);
            checkout(poorCustomer, expensiveCart);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}