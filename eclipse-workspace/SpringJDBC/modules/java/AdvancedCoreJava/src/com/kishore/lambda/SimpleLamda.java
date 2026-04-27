package com.kishore.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleLamda {
	
	public static void main(String[] args) {
	
	List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

	

	// Using streams and lambda expressions
	List<String> resultStreams = names.stream()
	                                  .filter(a -> a.startsWith("A"))
	                                  .map(String::toUpperCase)
	                                  .collect(Collectors.toList());
	System.out.println(resultStreams);
	}

}
