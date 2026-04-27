package com.kishore.futures;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureExceptionHandlingExample {
	public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            // Simulate an exception
            throw new RuntimeException("Exception in CompletableFuture");
        });

        // Handle exception using exceptionally
        CompletableFuture<String> exceptionHandledFuture = future.exceptionally(ex -> "Handled: " + ex.getMessage());

        // Attach a callback to the exceptionHandledFuture
        exceptionHandledFuture.thenAccept(result -> {
            System.out.println("Handled Result: " + result);
        });

        // Block and get the result when it's ready
        try {
            String result = exceptionHandledFuture.get();
            System.out.println("Final Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
