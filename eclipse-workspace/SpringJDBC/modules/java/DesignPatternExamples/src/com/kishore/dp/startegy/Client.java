package com.kishore.dp.startegy;

public class Client {
	public static void main(String[] args) {
        // Create instances of pricing strategies
        PricingStrategy normalPricing = new NormalPricingStrategy();
        PricingStrategy discountPricing = new DiscountPricingStrategy(10); // 10% discount

        // Create an instance of the BillingSystem
        BillingSystem billingSystem = new BillingSystem();

        // Use NormalPricingStrategy
        billingSystem.setPricingStrategy(normalPricing);
        double totalCostNormal = billingSystem.calculateTotalCost(100.0);
        System.out.println("Total Cost with Normal Pricing: $" + totalCostNormal);

        System.out.println("---------------------------");

        // Use DiscountPricingStrategy
        billingSystem.setPricingStrategy(discountPricing);
        double totalCostDiscount = billingSystem.calculateTotalCost(100.0);
        System.out.println("Total Cost with Discount Pricing: $" + totalCostDiscount);
    }

}
