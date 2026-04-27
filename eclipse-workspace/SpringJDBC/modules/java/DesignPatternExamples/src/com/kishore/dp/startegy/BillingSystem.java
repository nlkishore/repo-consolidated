package com.kishore.dp.startegy;

public class BillingSystem {
	private PricingStrategy pricingStrategy;

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public double calculateTotalCost(double originalPrice) {
        // Delegate the calculation to the selected pricing strategy
        return pricingStrategy.calculateTotalCost(originalPrice);
    }

}
