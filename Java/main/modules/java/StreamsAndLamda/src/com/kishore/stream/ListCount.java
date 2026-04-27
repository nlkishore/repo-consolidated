package com.kishore.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListCount {
	public static void main(String[] args) {
		List<String> strList = Arrays.asList("abc", "", "bcd", "", "defg", "jk"); 
		long count = strList.stream() .filter(x -> x.isEmpty()).count();
		System.out.println("List Count where no empty String "+count);
		// List containing String more than 3 charatcers
		long num = strList.stream() .filter(x -> x.length()> 3) .count();
		System.out.println("List Count where no empty String "+num);
		
		long wordCountWithA = strList.stream() .filter(x -> x.startsWith("a")) .count();
		System.out.println("List Word Count Starts with A "+wordCountWithA);
		
		// Remove Empty Strings
		List<String> filtered = strList.stream() .filter(x -> !x.isEmpty()) .collect(Collectors.toList());

		System.out.println("Filtered List Elements "+filtered);
		
		List<String> filtered2Char = strList.stream() .filter(x -> x.length()> 2) .collect(Collectors.toList());
		System.out.println("Filtered List Elements "+filtered2Char);
		
		
		List<String> G7 = Arrays.asList("USA", "Japan", "France", "Germany", "Italy", "U.K.","Canada"); 
		String G7Countries = G7.stream() .map(x -> x.toUpperCase()) .collect(Collectors.joining(", "));
		System.out.println("Upper Case G7 Countries "+G7);
		
		
		List<Integer> numbers = Arrays.asList(9, 10, 3, 4, 7, 3, 4); 
		List<Integer> distinct = numbers.stream() .map( i -> i*i) .distinct() .collect(Collectors.toList());
		System.out.println(" Distinct "+distinct);

		
	}

}
