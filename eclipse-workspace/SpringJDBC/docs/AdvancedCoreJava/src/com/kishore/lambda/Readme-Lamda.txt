Few java Example Using Lamda and Streams
ChatGPT
Certainly! Here are a few examples demonstrating the use of lambda expressions and streams in Java:

Example 1: List Manipulation using Streams and Lambda Expressions
java
Copy code
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
Example 2: Sum of Squares using Streams and Lambda Expressions
java
Copy code
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
Example 3: Filtering and Counting using Streams and Lambda Expressions
java
Copy code
import java.util.Arrays;

public class StreamAndLambdaCountExample {

    public static void main(String[] args) {
        // Using lambda expression to filter and count elements
        String[] words = {"Java", "Streams", "Lambda", "Example", "Count"};

        long count = Arrays.stream(words)
                .filter(word -> word.length() > 5)
                .count();

        System.out.println("Number of words with length > 5: " + count);
    }
}
Example 4: Sorting and Collecting using Streams and Lambda Expressions
java
Copy code
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

        System.out.println("Sorted Numbers: " + sortedNumbers);
    }
}
These examples showcase how lambda expressions and streams can be used for concise and expressive code in Java, especially when working with collections and transformations. Feel free to modify and expand upon them based on your specific needs.





Java Lamda sample  using  Person Object as example
ChatGPT
Certainly! Let's create a simple example using a Person class and demonstrate the use of lambda expressions for sorting and filtering a list of Person objects.

java
Copy code
import java.util.Arrays;
import java.util.List;

class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

public class LambdaPersonExample {

    public static void main(String[] args) {
        List<Person> people = Arrays.asList(
                new Person("Alice", 25),
                new Person("Bob", 30),
                new Person("Charlie", 22),
                new Person("David", 35)
        );

        // Sorting people by age using lambda expression
        people.sort((p1, p2) -> Integer.compare(p1.getAge(), p2.getAge()));

        System.out.println("Sorted People by Age:");
        people.forEach(System.out::println);

        // Filtering people over 25 years old using lambda expression
        List<Person> filteredPeople = people.stream()
                .filter(person -> person.getAge() > 25)
                .toList();  // Requires Java 16+

        System.out.println("\nPeople over 25 years old:");
        filteredPeople.forEach(System.out::println);
    }
}
In this example:

We have a Person class with a name and age.
We create a list of Person objects.
We use a lambda expression to sort the list of people based on their age.
We use a lambda expression and the filter method to create a new list of people who are older than 25.
Please note that the toList() method is used to collect the filtered stream into a List, and it requires Java 16 or later. If you are using an earlier version of Java, you can replace it with collect(Collectors.toList()).



