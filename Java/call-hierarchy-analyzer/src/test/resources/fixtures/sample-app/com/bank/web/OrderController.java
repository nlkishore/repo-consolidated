package com.bank.web;

import com.bank.service.OrderService;
import com.bank.shared.SharedAudit;

public class OrderController {
  private final OrderService orderService = new OrderService();
  private final SharedAudit audit = new SharedAudit();

  public void createOrder(String request) {
    orderService.place(request);
    audit.log("created:" + request);
  }
}
