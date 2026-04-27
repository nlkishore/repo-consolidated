package com.kishore.dp.chainofresponsibility;

public class Client {
	public static void main(String[] args) {
        // Create instances of handlers
//        PurchaseHandler manager = new Manager();
//        PurchaseHandler director = new Director();
//        PurchaseHandler vicePresident = new VicePresident();

		Manager manager = new Manager();
		Director director=new Director();
		VicePresident vicePresident = new VicePresident();
        // Set up the chain of responsibility
        manager.setNextHandler(director);
        director.setNextHandler(vicePresident);

        // Create a purchase request
        PurchaseRequest request1 = new PurchaseRequest(800.0);
        PurchaseRequest request2 = new PurchaseRequest(5000.0);
        PurchaseRequest request3 = new PurchaseRequest(12000.0);

        // Process purchase requests
        manager.handleRequest(request1);
        System.out.println("----------------------");
        manager.handleRequest(request2);
        System.out.println("----------------------");
        manager.handleRequest(request3);
    }

}
