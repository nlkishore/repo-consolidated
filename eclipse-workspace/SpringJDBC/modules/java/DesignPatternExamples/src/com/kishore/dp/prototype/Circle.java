package com.kishore.dp.prototype;

public class Circle implements Shape {

	private String color;
    private int radius;

    public Circle(String color, int radius) {
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Circle - Color: " + color + ", Radius: " + radius);
    }

    @Override
    public Shape clone() {
        return new Circle(color, radius);
    }

}
