package com.kishore.dp.singleton;

//Client code
public class Client {
 public static void main(String[] args) {
     // Get the Singleton instance
     Singleton singletonInstance1 = Singleton.getInstance();

     // Call a method on the Singleton instance
     singletonInstance1.doSomething();

     // Try to create another instance (won't work)
     // Singleton singletonInstance2 = new Singleton(); // Compiler error

     // Get the Singleton instance again (returns the same instance)
     Singleton singletonInstance3 = Singleton.getInstance();

     // Check if both instances refer to the same object
     System.out.println("Are both instances the same? " + (singletonInstance1 == singletonInstance3));
 }
}
