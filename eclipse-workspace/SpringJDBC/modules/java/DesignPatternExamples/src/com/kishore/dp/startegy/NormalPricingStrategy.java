package com.kishore.dp.startegy;

public class NormalPricingStrategy implements PricingStrategy {

	@Override
    public double calculateTotalCost(double originalPrice) {
        // No discount for normal pricing
        return originalPrice;
    }

}
