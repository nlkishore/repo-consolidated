package com.kishore.futures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureExample {
	 public static void main(String[] args) {
	        // Create a CompletableFuture
	        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
	            // Simulate a time-consuming task
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	            return "Hello, CompletableFuture!";
	        });

	        // Attach a callback to the CompletableFuture
	        future.thenAccept(result -> {
	            System.out.println("Callback: " + result);
	        });

	        // Perform other tasks while waiting for the CompletableFuture to complete

	        // Block and get the result when it's ready
	        try {
	            String result = future.get();
	            System.out.println("Result: " + result);
	        } catch (InterruptedException | ExecutionException e) {
	            e.printStackTrace();
	        }
	    }
}
