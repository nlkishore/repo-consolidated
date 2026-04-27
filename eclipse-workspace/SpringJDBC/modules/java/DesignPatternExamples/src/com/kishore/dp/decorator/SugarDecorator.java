package com.kishore.dp.decorator;

public class SugarDecorator extends CoffeeDecorator {

	public SugarDecorator(Coffee decoratedCoffee) {
		super(decoratedCoffee);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    public String getDescription() {
        return super.getDescription() + " with Sugar";
    }

    @Override
    public double cost() {
        return super.cost() + 0.2; // Additional cost for sugar
    }

}
