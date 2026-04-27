package com.kishore.dp.chainofresponsibility;

public class Manager implements PurchaseHandler {
	
	private static final double MANAGER_LIMIT = 1000.0;
    
	private PurchaseHandler nextHandler;

    public void setNextHandler(PurchaseHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= MANAGER_LIMIT) {
            System.out.println("Manager approves the purchase request of $" + request.getAmount());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }

}
