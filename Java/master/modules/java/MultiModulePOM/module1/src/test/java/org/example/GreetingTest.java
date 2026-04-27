package org.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GreetingTest {
    @Test
    public void testSayHello() {
        Greeting greeting = new Greeting();
        assertEquals("Hello, John!", greeting.sayHello("John"));
    }
}
