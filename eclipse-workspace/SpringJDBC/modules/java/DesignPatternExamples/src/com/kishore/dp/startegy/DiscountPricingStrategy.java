package com.kishore.dp.startegy;

public class DiscountPricingStrategy implements PricingStrategy {

	private double discountPercentage;

    public DiscountPricingStrategy(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double calculateTotalCost(double originalPrice) {
        // Apply a discount to the original price
        return originalPrice * (1 - discountPercentage / 100);
    }

}
