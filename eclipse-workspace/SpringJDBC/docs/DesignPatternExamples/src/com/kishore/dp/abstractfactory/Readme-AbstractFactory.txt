Java Example using abstract Factory Pattern
ChatGPT
The Abstract Factory Pattern is a creational design pattern that provides an interface for creating families of related or dependent objects without specifying their concrete classes. This pattern ensures that a family of related or dependent objects can be created, configured, and used together. Let's consider a simple example in Java where we use the Abstract Factory Pattern to create abstract products (Chair and Sofa) and concrete factories for two different styles of furniture (ModernFurnitureFactory and VictorianFurnitureFactory).

java
Copy code
// Abstract Product: Chair
interface Chair {
    void sitOn();
}

// Concrete Product 1: ModernChair
class ModernChair implements Chair {
    @Override
    public void sitOn() {
        System.out.println("Sitting on a modern chair.");
    }
}

// Concrete Product 2: VictorianChair
class VictorianChair implements Chair {
    @Override
    public void sitOn() {
        System.out.println("Sitting on a Victorian chair.");
    }
}

// Abstract Product: Sofa
interface Sofa {
    void relax();
}

// Concrete Product 1: ModernSofa
class ModernSofa implements Sofa {
    @Override
    public void relax() {
        System.out.println("Relaxing on a modern sofa.");
    }
}

// Concrete Product 2: VictorianSofa
class VictorianSofa implements Sofa {
    @Override
    public void relax() {
        System.out.println("Relaxing on a Victorian sofa.");
    }
}

// Abstract Factory
interface FurnitureFactory {
    Chair createChair();
    Sofa createSofa();
}

// Concrete Factory 1: ModernFurnitureFactory
class ModernFurnitureFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new ModernChair();
    }

    @Override
    public Sofa createSofa() {
        return new ModernSofa();
    }
}

// Concrete Factory 2: VictorianFurnitureFactory
class VictorianFurnitureFactory implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new VictorianChair();
    }

    @Override
    public Sofa createSofa() {
        return new VictorianSofa();
    }
}

// Client code
public class Client {
    public static void main(String[] args) {
        // Create a modern furniture factory
        FurnitureFactory modernFactory = new ModernFurnitureFactory();

        // Create modern chair and sofa
        Chair modernChair = modernFactory.createChair();
        Sofa modernSofa = modernFactory.createSofa();

        // Use modern furniture
        modernChair.sitOn();
        modernSofa.relax();

        System.out.println("---------------------------");

        // Create a Victorian furniture factory
        FurnitureFactory victorianFactory = new VictorianFurnitureFactory();

        // Create Victorian chair and sofa
        Chair victorianChair = victorianFactory.createChair();
        Sofa victorianSofa = victorianFactory.createSofa();

        // Use Victorian furniture
        victorianChair.sitOn();
        victorianSofa.relax();
    }
}
In this example:

Chair and Sofa are abstract product interfaces representing furniture items.
ModernChair, VictorianChair, ModernSofa, and VictorianSofa are concrete product classes implementing the Chair and Sofa interfaces.
FurnitureFactory is the abstract factory interface defining methods to create Chair and Sofa objects.
ModernFurnitureFactory and VictorianFurnitureFactory are concrete factory classes implementing the FurnitureFactory interface. Each concrete factory creates a family of related products with a specific style.
The client code demonstrates how to use the abstract factory pattern by creating a specific factory (ModernFurnitureFactory or VictorianFurnitureFactory) and using it to create chairs and sofas with consistent styles.
The abstract factory pattern allows for the creation of families of related or dependent objects without specifying their concrete classes. This promotes flexibility and ensures that the created objects are compatible with each other.