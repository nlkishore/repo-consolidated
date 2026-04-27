package com.example;

public class CustomerService {
    private OrderService orderService = new OrderService();

    public double processOrder(Order order) {
        return orderService.calculateTotal(order);
    }
}
