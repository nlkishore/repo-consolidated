package com.validator;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import java.io.StringReader;

public class JsonWellFormednessChecker {

    public static boolean isJsonWellFormed(String jsonString) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
            // Attempting to read the JSON
            reader.read();
            return true; // If reading succeeds, JSON is well-formed
        } catch (JsonException e) {
            // Catching JsonException if JSON is not well-formed
            return false;
        }
    }

    public static void main(String[] args) {
        String wellFormedJson = "{\"name\": \"John\", \"age\": 25}";
        String malformedJson = "{\"name\": \"John\", \"age\": 25,}"; // Extra comma for malformation

        System.out.println("Is well-formed JSON: " + isJsonWellFormed(wellFormedJson)); // Should print: true
        System.out.println("Is well-formed JSON: " + isJsonWellFormed(malformedJson));   // Should print: false
    }
}

