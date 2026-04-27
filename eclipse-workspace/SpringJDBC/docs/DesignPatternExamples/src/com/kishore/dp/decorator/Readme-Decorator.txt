Java Example using Decorator Pattern
ChatGPT
The Decorator Pattern is a structural design pattern that allows behavior to be added to an individual object, either statically or dynamically, without affecting the behavior of other objects from the same class. This pattern is useful for extending the functionalities of classes in a flexible and reusable way.

Let's consider a simple example where we have a Coffee class representing a basic coffee. We want to decorate this coffee with additional features such as milk and sugar using the Decorator Pattern.

java
Copy code
// Component: Coffee
interface Coffee {
    String getDescription();

    double cost();
}

// Concrete Component: BasicCoffee
class BasicCoffee implements Coffee {
    @Override
    public String getDescription() {
        return "Basic Coffee";
    }

    @Override
    public double cost() {
        return 2.0; // Basic coffee cost
    }
}

// Decorator: CoffeeDecorator
abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;

    public CoffeeDecorator(Coffee decoratedCoffee) {
        this.decoratedCoffee = decoratedCoffee;
    }

    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }

    @Override
    public double cost() {
        return decoratedCoffee.cost();
    }
}

// Concrete Decorator: MilkDecorator
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee decoratedCoffee) {
        super(decoratedCoffee);
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

// Concrete Decorator: SugarDecorator
class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee decoratedCoffee) {
        super(decoratedCoffee);
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

// Client code
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
In this example:

The Coffee interface represents the component interface.
The BasicCoffee class is a concrete component that implements the Coffee interface.
The CoffeeDecorator is an abstract decorator class that implements the Coffee interface and contains a reference to the decorated coffee.
The MilkDecorator and SugarDecorator are concrete decorator classes that extend CoffeeDecorator and add specific functionalities.
The Client class demonstrates how to create a basic coffee and decorate it with milk and sugar.
The Decorator Pattern allows you to add new functionalities to objects dynamically at runtime without altering their structure. It promotes an open-closed principle by allowing extension without modifying existing code.