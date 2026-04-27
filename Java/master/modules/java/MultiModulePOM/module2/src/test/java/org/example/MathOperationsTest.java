package org.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MathOperationsTest {
    @Test
    public void testMultiply() {
        MathOperations math = new MathOperations();
        assertEquals(6, math.multiply(2, 3));
    }
}

