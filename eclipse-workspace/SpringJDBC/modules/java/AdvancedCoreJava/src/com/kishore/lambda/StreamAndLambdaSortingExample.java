package com.kishore.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamAndLambdaSortingExample {
	public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 3);

        // Using lambda expression to sort and collect elements
        List<Integer> sortedNumbers = numbers.stream()
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
        
     // Using lambda expression to sort and collect elements
        List<Integer> sortedNumbers1 = numbers.stream()
                .sorted((a, b) -> b.compareTo(a))
                .toList();

        System.out.println("Sorted Numbers: " + sortedNumbers);
        System.out.println("Sorted Numbers: " + sortedNumbers1);
    }
}
