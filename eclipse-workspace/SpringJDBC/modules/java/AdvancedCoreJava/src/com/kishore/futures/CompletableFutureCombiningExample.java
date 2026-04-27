package com.kishore.futures;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureCombiningExample {
	public static void main(String[] args) {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> " World");

        // Combine two CompletableFutures using thenCombine
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (s1, s2) -> s1 + s2);

        // Attach a callback to the combined CompletableFuture
        combinedFuture.thenAccept(result -> {
            System.out.println("Combined Result: " + result);
        });

        // Block and get the result when it's ready
        try {
            String result = combinedFuture.get();
            System.out.println("Final Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
