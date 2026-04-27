Java Example using futures
ChatGPT
Certainly! In Java, CompletableFuture is often used to work with futures. It's a part of the java.util.concurrent package and provides a way to express asynchronous computations with callbacks.

Here's a simple example using CompletableFuture:

java
Copy code
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
In this example:

We create a CompletableFuture using supplyAsync, which performs a task asynchronously and returns a result.
We attach a callback using thenAccept that prints the result when the CompletableFuture completes.
We can perform other tasks while waiting for the CompletableFuture to complete.
Finally, we block and get the result when it's ready.
Note: Using CompletableFuture is just one way to work with futures in Java. Java also has the Future interface in the java.util.concurrent package, which is simpler but less feature-rich compared to CompletableFuture. Depending on your requirements, you might choose the one that fits your needs better.




Few More Examples using java futures
ChatGPT
Certainly! Here are a few more examples using CompletableFuture in Java:

Example 1: Chaining Multiple CompletableFutures
java
Copy code
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
Example 2: Combining Multiple CompletableFutures
java
Copy code
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
Example 3: Exception Handling with CompletableFutures
java
Copy code
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
These examples demonstrate various features of CompletableFuture, including chaining, combining, and exception handling. You can explore more functionalities based on your specific use cases.





