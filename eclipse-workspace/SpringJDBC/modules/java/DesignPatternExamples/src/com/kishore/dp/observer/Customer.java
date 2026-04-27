package com.kishore.dp.observer;

public class Customer implements ProductObserver {
	
	private String name;

    public Customer(String name) {
        this.name = name;
    }

	@Override
	public void update(Product product) {
		System.out.println("Customer " + name + " notified about the price change:");
        System.out.println("Product: " + product.getProductName() + ", New Price: $" + product.getPrice());
        System.out.println("--------------------------------------------------");

	}

}
