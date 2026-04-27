package com.example;

import com.example.model.ComplexObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ComplexObjectTest {

    private ComplexObject complexObject;

    @BeforeEach
    public void setUp() {
        complexObject = new ComplexObject("John Doe", 30, "123 Main St", true);
    }

    @Test
    public void testValidObjectCreation() {
        ComplexObject obj = new ComplexObject("Alice", 25, "456 Elm St", false);
        assertNotNull(obj);
    }

    // Exception Tests for Constructor
    @Test
    public void testConstructor_NullName_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ComplexObject(null, 30, "123 Main St", true)
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConstructor_EmptyName_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ComplexObject("", 30, "123 Main St", true)
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testConstructor_InvalidAge_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ComplexObject("John", -5, "123 Main St", true)
        );
        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    public void testConstructor_NullAddress_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new ComplexObject("John", 30, null, true)
        );
        assertEquals("Address cannot be null or empty", exception.getMessage());
    }

    // Exception Tests for Setters
    @Test
    public void testSetName_NullValue_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setName(null)
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testSetName_EmptyValue_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setName("")
        );
        assertEquals("Name cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testSetAge_NegativeValue_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setAge(-1)
        );
        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    public void testSetAge_OverLimit_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setAge(200)
        );
        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    public void testSetAddress_NullValue_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setAddress(null)
        );
        assertEquals("Address cannot be null or empty", exception.getMessage());
    }

    @Test
    public void testSetAddress_EmptyValue_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                complexObject.setAddress("")
        );
        assertEquals("Address cannot be null or empty", exception.getMessage());
    }

    // Test Getters and Setters
    @Test
    public void testGetName() {
        assertEquals("John Doe", complexObject.getName());
    }

    @Test
    public void testSetName() {
        complexObject.setName("Jane Doe");
        assertEquals("Jane Doe", complexObject.getName());
    }

    @Test
    public void testGetAge() {
        assertEquals(30, complexObject.getAge());
    }

    @Test
    public void testSetAge() {
        complexObject.setAge(35);
        assertEquals(35, complexObject.getAge());
    }

    @Test
    public void testGetAddress() {
        assertEquals("123 Main St", complexObject.getAddress());
    }

    @Test
    public void testSetAddress() {
        complexObject.setAddress("456 Oak St");
        assertEquals("456 Oak St", complexObject.getAddress());
    }

    @Test
    public void testIsActive() {
        assertTrue(complexObject.isActive());
    }

    @Test
    public void testSetActive() {
        complexObject.setActive(false);
        assertFalse(complexObject.isActive());
    }

    @Test
    public void testToString() {
        String expected = "ComplexObject{name='John Doe', age=30, address='123 Main St', isActive=true}";
        assertEquals(expected, complexObject.toString());
    }
}
