package com.kishore.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamAndLambdaCountExample {
	public static void main(String[] args) {
        // Using lambda expression to filter and count elements
        String[] words = {"Java", "Streams", "Lambda", "Example", "Count"};

        long count = Arrays.stream(words)
                .filter(word -> word.length() > 5)
                .count();

        List<String> _5CharWord = Arrays.stream(words)
                .filter(word -> word.length() > 5)
                .collect(Collectors.toList());
        
       
        System.out.println("Number of words with length > 5: " + count);
        System.out.println("List of words with length > 5: " + _5CharWord);
    }
}
