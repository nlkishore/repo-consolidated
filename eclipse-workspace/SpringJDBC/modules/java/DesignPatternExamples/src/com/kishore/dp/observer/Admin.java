package com.kishore.dp.observer;

public class Admin implements ProductObserver {

	@Override
    public void update(Product product) {
        System.out.println("Admin notified about the price change:");
        System.out.println("Product: " + product.getProductName() + ", New Price: $" + product.getPrice());
        System.out.println("--------------------------------------------------");
    }

}
