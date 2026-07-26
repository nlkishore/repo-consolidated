package com.bank.service;

import com.bank.shared.SharedAudit;

public class OrderService {
  private final SharedAudit audit = new SharedAudit();

  public void place(String request) {
    audit.log("place:" + request);
    save(request);
  }

  private void save(String request) {
    audit.log("save:" + request);
  }
}
