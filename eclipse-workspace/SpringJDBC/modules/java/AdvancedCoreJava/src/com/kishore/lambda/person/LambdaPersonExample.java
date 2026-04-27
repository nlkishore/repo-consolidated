package com.kishore.lambda.person;

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

