package com.kishore.dp.prototype;

import java.util.HashMap;
import java.util.Map;

public class Client {
	public static void main(String[] args) {
        // Create prototype objects
        Shape circlePrototype = new Circle("Red", 10);
        Shape rectanglePrototype = new Rectangle("Blue", 20, 15);

        // Create a prototype registry
        Map<String, Shape> shapeRegistry = new HashMap<>();
        shapeRegistry.put("Circle", circlePrototype);
        shapeRegistry.put("Rectangle", rectanglePrototype);

        // Clone and draw shapes from the registry
        Shape clonedCircle = shapeRegistry.get("Circle").clone();
        clonedCircle.draw();

        Shape clonedRectangle = shapeRegistry.get("Rectangle").clone();
        clonedRectangle.draw();
    }
}
