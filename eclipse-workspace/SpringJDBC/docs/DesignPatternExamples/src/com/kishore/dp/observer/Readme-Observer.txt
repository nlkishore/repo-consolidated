Java Example using Observer Pattern
ChatGPT
The Observer Pattern is a behavioral design pattern where an object, known as the subject, maintains a list of dependents, known as observers, that are notified of any state changes, typically by calling one of their methods. This pattern is widely used for implementing distributed event handling systems.

Let's consider a simple example of an online store where customers can subscribe to notifications for product updates. The Product class represents a product, and the ProductObserver interface defines the methods to be implemented by observers.

java
Copy code
import java.util.ArrayList;
import java.util.List;

// Subject: Observable (Product)
class Product {
    private String productName;
    private double price;
    private List<ProductObserver> observers = new ArrayList<>();

    public Product(String productName, double price) {
        this.productName = productName;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        notifyObservers(); // Notify observers about the price change
    }

    // Observer management methods
    public void addObserver(ProductObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ProductObserver observer) {
        observers.remove(observer);
    }

    // Notify observers about the state change (price change)
    private void notifyObservers() {
        for (ProductObserver observer : observers) {
            observer.update(this);
        }
    }
}

// Observer: Observer (ProductObserver)
interface ProductObserver {
    void update(Product product);
}

// Concrete Observer: Concrete Observer 1 (Customer)
class Customer implements ProductObserver {
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

// Concrete Observer: Concrete Observer 2 (Admin)
class Admin implements ProductObserver {
    @Override
    public void update(Product product) {
        System.out.println("Admin notified about the price change:");
        System.out.println("Product: " + product.getProductName() + ", New Price: $" + product.getPrice());
        System.out.println("--------------------------------------------------");
    }
}

// Client code
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
In this example:

The Product class is the subject (observable) that maintains a list of observers.
The ProductObserver interface declares the update method to be implemented by concrete observers.
Concrete observers (Customer and Admin) implement the ProductObserver interface and define their specific update behavior.
The Client class demonstrates how to create a product, register observers, and trigger notifications by changing the product's price.
When the price of the product changes, all registered observers are notified, and they perform their specific update actions. The Observer Pattern allows for a flexible and decoupled way to implement distributed event handling systems.