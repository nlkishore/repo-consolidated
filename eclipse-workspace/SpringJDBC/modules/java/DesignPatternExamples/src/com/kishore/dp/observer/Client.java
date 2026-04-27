package com.kishore.dp.observer;

public class Client {
	
	 public static void main(String[] args) {
	        // Create a product
	        Product laptop = new Product("Laptop", 1000.0);

	        // Create observers (customers and admin)
	        ProductObserver customer1 = new Customer("Alice");
	        ProductObserver customer2 = new Customer("Bob");
	        ProductObserver admin = new Admin();

	        // Register observers
	        laptop.addObserver(customer1);
	        laptop.addObserver(customer2);
	        laptop.addObserver(admin);

	        // Change the price of the product, which will notify the observers
	        laptop.setPrice(1200.0);
	    }

}
