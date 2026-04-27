package com.kishore.lambda;

import java.util.Arrays;

public class StreamAndLambdaSumExample {
	 public static void main(String[] args) {
	        // Using lambda expression to calculate the sum of squares
	        int[] numbers = {1, 2, 3, 4, 5};
	        int sumOfSquares = Arrays.stream(numbers)
	                .map(x -> x * x)
	                .sum();

	        System.out.println("Sum of Squares: " + sumOfSquares);
	    }
}
