package com.kishore.dp.singleton;

public class Singleton {
    // Private static instance variable
    private static Singleton instance;

    // Private constructor to prevent instantiation from outside the class
    private Singleton() {
        // Initialization code if needed
    }

    // Public static method to provide the global point of access to the Singleton instance
    public static Singleton getInstance() {
        // Lazy initialization: create the instance only if it doesn't exist yet
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    // Other methods and properties of the Singleton class can be added here
    public void doSomething() {
        System.out.println("Singleton instance is doing something.");
    }
}
