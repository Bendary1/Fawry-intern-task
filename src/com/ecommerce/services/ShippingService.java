package com.ecommerce.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ecommerce.interfaces.Shippable;

public class ShippingService {
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