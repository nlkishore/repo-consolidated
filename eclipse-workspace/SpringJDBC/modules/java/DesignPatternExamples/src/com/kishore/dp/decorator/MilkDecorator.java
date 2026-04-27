package com.kishore.dp.decorator;

public class MilkDecorator extends CoffeeDecorator {

	public MilkDecorator(Coffee decoratedCoffee) {
		super(decoratedCoffee);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public String getDescription() {
        return super.getDescription() + " with Milk";
    }

    @Override
    public double cost() {
        return super.cost() + 0.5; // Additional cost for milk
    }

}
