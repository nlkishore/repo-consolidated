package com.kishore.futures;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureChainingExample {
	public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");

        // Chain CompletableFuture using thenApply
        CompletableFuture<String> chainedFuture = future.thenApply(s -> s + " World");

        // Attach a callback to the chained CompletableFuture
        chainedFuture.thenAccept(result -> {
            System.out.println("Chained Result: " + result);
        });

        // Block and get the result when it's ready
        try {
            String result = chainedFuture.get();
            System.out.println("Final Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
