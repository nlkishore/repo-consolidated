package com.kishore.dp.chainofresponsibility;

public class Director implements PurchaseHandler {
	
	private static final double DIRECTOR_LIMIT = 5000.0;
    private PurchaseHandler nextHandler;

    public void setNextHandler(PurchaseHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= DIRECTOR_LIMIT) {
            System.out.println("Director approves the purchase request of $" + request.getAmount());
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }

}
