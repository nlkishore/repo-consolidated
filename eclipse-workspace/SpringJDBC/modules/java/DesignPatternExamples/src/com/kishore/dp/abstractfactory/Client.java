package com.kishore.dp.abstractfactory;

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
