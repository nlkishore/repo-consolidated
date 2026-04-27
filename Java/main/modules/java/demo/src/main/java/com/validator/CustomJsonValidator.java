package com.validator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;

public class CustomJsonValidator {

    public static boolean validateJson(String jsonString) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
            JsonObject jsonObject = reader.readObject();
            return validateJsonObject(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean validateJsonObject(JsonObject jsonObject) {
        // Your custom validation logic goes here
        JsonValue nameValue = jsonObject.get("name");
        JsonValue ageValue = jsonObject.get("age");

        return nameValue != null && nameValue.getValueType() == JsonValue.ValueType.STRING
                && ageValue != null && ageValue.getValueType() == JsonValue.ValueType.NUMBER;
    }

    public static void main(String[] args) {
        String validJson = "{\"name\": \"John\", \"age\": 25}";
        String invalidJson = "{\"name\": 123, \"age\": \"twenty\"}";

        System.out.println("Is valid JSON: " + validateJson(validJson)); // Should print: true
        System.out.println("Is valid JSON: " + validateJson(invalidJson)); // Should print: false
    }
}

