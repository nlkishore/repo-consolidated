package com.kishore.dp.decorator;

public class BasicCoffee implements Coffee {

	@Override
    public String getDescription() {
        return "Basic Coffee";
    }

    @Override
    public double cost() {
        return 2.0; // Basic coffee cost
    }

}
