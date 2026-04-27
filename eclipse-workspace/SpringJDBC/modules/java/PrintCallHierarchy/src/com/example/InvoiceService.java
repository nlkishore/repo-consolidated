package com.example;

public class InvoiceService {
    private CustomerService customerService = new CustomerService();

    public void generateInvoice(Order order) {
        double total = customerService.processOrder(order);
        // Further processing
    }
}
