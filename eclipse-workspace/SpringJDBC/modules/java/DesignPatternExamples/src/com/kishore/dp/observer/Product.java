package com.kishore.dp.observer;

import java.util.ArrayList;
import java.util.List;

//Subject: Observable (Product)
class Product {
 private String productName;
 private double price;
 private List<ProductObserver> observers = new ArrayList<>();

 public Product(String productName, double price) {
     this.productName = productName;
     this.price = price;
 }

 public String getProductName() {
     return productName;
 }

 public double getPrice() {
     return price;
 }

 public void setPrice(double price) {
     this.price = price;
     notifyObservers(); // Notify observers about the price change
 }

 // Observer management methods
 public void addObserver(ProductObserver observer) {
     observers.add(observer);
 }

 public void removeObserver(ProductObserver observer) {
     observers.remove(observer);
 }

 // Notify observers about the state change (price change)
 private void notifyObservers() {
     for (ProductObserver observer : observers) {
         observer.update(this);
     }
 }
}
