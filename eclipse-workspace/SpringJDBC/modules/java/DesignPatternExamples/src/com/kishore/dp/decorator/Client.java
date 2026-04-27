package com.kishore.dp.decorator;

public class Client {
	public static void main(String[] args) {
        // Create a basic coffee
        Coffee basicCoffee = new BasicCoffee();
        System.out.println("Basic Coffee:");
        System.out.println("Description: " + basicCoffee.getDescription());
        System.out.println("Cost: $" + basicCoffee.cost());
        System.out.println();

        // Decorate the basic coffee with milk
        Coffee milkCoffee = new MilkDecorator(basicCoffee);
        System.out.println("Coffee with Milk:");
        System.out.println("Description: " + milkCoffee.getDescription());
        System.out.println("Cost: $" + milkCoffee.cost());
        System.out.println();

        // Decorate the coffee with milk further with sugar
        Coffee milkSugarCoffee = new SugarDecorator(milkCoffee);
        System.out.println("Coffee with Milk and Sugar:");
        System.out.println("Description: " + milkSugarCoffee.getDescription());
        System.out.println("Cost: $" + milkSugarCoffee.cost());
    }

}
