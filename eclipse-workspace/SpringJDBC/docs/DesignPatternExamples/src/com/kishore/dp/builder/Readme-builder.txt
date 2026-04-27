Java Example of Builder pattern
ChatGPT
The Builder Pattern is a creational design pattern that allows the construction of a complex object step by step. It separates the construction of a complex object from its representation, allowing the same construction process to create different representations. Let's consider a simple example in Java where we use the Builder Pattern to construct a Computer object.

java
Copy code
// Product: Computer
class Computer {
    private String cpu;
    private String memory;
    private String storage;
    private String graphicsCard;

    public Computer(String cpu, String memory, String storage, String graphicsCard) {
        this.cpu = cpu;
        this.memory = memory;
        this.storage = storage;
        this.graphicsCard = graphicsCard;
    }

    // Getter methods for the properties

    @Override
    public String toString() {
        return "Computer{" +
                "cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                ", storage='" + storage + '\'' +
                ", graphicsCard='" + graphicsCard + '\'' +
                '}';
    }
}

// Builder interface
interface ComputerBuilder {
    ComputerBuilder setCpu(String cpu);
    ComputerBuilder setMemory(String memory);
    ComputerBuilder setStorage(String storage);
    ComputerBuilder setGraphicsCard(String graphicsCard);
    Computer build();
}

// Concrete Builder: DesktopComputerBuilder
class DesktopComputerBuilder implements ComputerBuilder {
    private Computer computer;

    public DesktopComputerBuilder() {
        this.computer = new Computer("Default CPU", "Default Memory", "Default Storage", "Default Graphics Card");
    }

    @Override
    public ComputerBuilder setCpu(String cpu) {
        computer.cpu = cpu;
        return this;
    }

    @Override
    public ComputerBuilder setMemory(String memory) {
        computer.memory = memory;
        return this;
    }

    @Override
    public ComputerBuilder setStorage(String storage) {
        computer.storage = storage;
        return this;
    }

    @Override
    public ComputerBuilder setGraphicsCard(String graphicsCard) {
        computer.graphicsCard = graphicsCard;
        return this;
    }

    @Override
    public Computer build() {
        return computer;
    }
}

// Director: ComputerAssembler
class ComputerAssembler {
    private ComputerBuilder computerBuilder;

    public ComputerAssembler(ComputerBuilder computerBuilder) {
        this.computerBuilder = computerBuilder;
    }

    public Computer assembleComputer() {
        return computerBuilder.build();
    }
}

// Client code
public class Client {
    public static void main(String[] args) {
        // Create a DesktopComputerBuilder
        ComputerBuilder desktopComputerBuilder = new DesktopComputerBuilder();

        // Create a ComputerAssembler with the DesktopComputerBuilder
        ComputerAssembler computerAssembler = new ComputerAssembler(desktopComputerBuilder);

        // Assemble a computer with custom specifications
        Computer customComputer = computerAssembler
                .setCpu("Intel i7")
                .setMemory("16GB RAM")
                .setStorage("1TB SSD")
                .setGraphicsCard("NVIDIA GeForce RTX 3080")
                .assembleComputer();

        // Display the assembled computer
        System.out.println("Custom Computer Specifications:");
        System.out.println(customComputer);
    }
}
In this example:

The Computer class is the product that we want to build.
The ComputerBuilder interface defines the steps to build a Computer. Concrete builders, like DesktopComputerBuilder, implement this interface.
The DesktopComputerBuilder class is a concrete builder that constructs a Computer with default values and allows customization through setter methods.
The ComputerAssembler class acts as a director that orchestrates the construction process using a specific builder.
The client code demonstrates how to use the Builder Pattern to create a custom computer with specific specifications. The builder pattern allows for a flexible and step-by-step construction process, and it is especially useful when dealing with objects that have many optional parameters.