package com.kishore.dp.builder;

public class Client {
	public static void main(String[] args) {
        // Create a DesktopComputerBuilder
        ComputerBuilder desktopComputerBuilder = new DesktopComputerBuilder();

        // Create a ComputerAssembler with the DesktopComputerBuilder
        ComputerAssembler computerAssembler = new ComputerAssembler(desktopComputerBuilder);
        
        // Assemble a computer with custom specifications
        Computer customComputer = computerAssembler.getComputerBuilder()
                .setCpu("Intel i7")
                .setMemory("16GB RAM")
                .setStorage("1TB SSD")
                .setGraphicsCard("NVIDIA GeForce RTX 3080").build();
                

        
        // Display the assembled computer
        System.out.println("Custom Computer Specifications:");
        System.out.println(customComputer);
    }

}
