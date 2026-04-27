Java Example of Prototype pattern
ChatGPT
The Prototype Pattern is a creational design pattern that allows creating new objects by copying an existing object, known as the prototype. This pattern is useful when the cost of creating an object is more expensive or complex than copying an existing one. Let's consider a simple example in Java where we use the Prototype Pattern to create clones of shapes.

java
Copy code
import java.util.HashMap;
import java.util.Map;

// Prototype interface
interface Shape extends Cloneable {
    void draw();

    Shape clone();
}

// Concrete Prototype 1: Circle
class Circle implements Shape {
    private String color;
    private int radius;

    public Circle(String color, int radius) {
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Circle - Color: " + color + ", Radius: " + radius);
    }

    @Override
    public Shape clone() {
        return new Circle(color, radius);
    }
}

// Concrete Prototype 2: Rectangle
class Rectangle implements Shape {
    private String color;
    private int width;
    private int height;

    public Rectangle(String color, int width, int height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Rectangle - Color: " + color + ", Width: " + width + ", Height: " + height);
    }

    @Override
    public Shape clone() {
        return new Rectangle(color, width, height);
    }
}

// Client code
public class Client {
    public static void main(String[] args) {
        // Create prototype objects
        Shape circlePrototype = new Circle("Red", 10);
        Shape rectanglePrototype = new Rectangle("Blue", 20, 15);

        // Create a prototype registry
        Map<String, Shape> shapeRegistry = new HashMap<>();
        shapeRegistry.put("Circle", circlePrototype);
        shapeRegistry.put("Rectangle", rectanglePrototype);

        // Clone and draw shapes from the registry
        Shape clonedCircle = shapeRegistry.get("Circle").clone();
        clonedCircle.draw();

        Shape clonedRectangle = shapeRegistry.get("Rectangle").clone();
        clonedRectangle.draw();
    }
}
In this example:

The Shape interface is the prototype interface with the draw method for drawing shapes and the clone method for creating clones of shapes.
Circle and Rectangle are concrete prototype classes that implement the Shape interface. Each class provides its own implementation of the draw method and the clone method to create clones of the respective shapes.
The Client class demonstrates how to use the Prototype Pattern. It creates prototype objects (circle and rectangle), adds them to a prototype registry, and then clones and draws shapes from the registry.
The Prototype Pattern allows for the creation of new objects by copying existing ones, providing a mechanism for creating objects with specific configurations without the need to explicitly create each object from scratch.