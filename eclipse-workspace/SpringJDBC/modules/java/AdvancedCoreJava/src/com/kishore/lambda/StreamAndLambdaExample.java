package com.kishore.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamAndLambdaExample {
	public static void main(String[] args) {
        List<String> fruits = Arrays.asList("Apple", "Banana", "Orange", "Grapes", "Kiwi");

        // Using lambda expression to filter and transform elements
        List<String> filteredAndUpperCaseFruits = fruits.stream()
                .filter(fruit -> fruit.length() > 5)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        System.out.println("Filtered and UpperCase Fruits: " + filteredAndUpperCaseFruits);
    }
}
