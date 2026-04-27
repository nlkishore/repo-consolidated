package com.kishore.dp.chainofresponsibility;

public class VicePresident implements PurchaseHandler {
	private static final double VICE_PRESIDENT_LIMIT = 10000.0;

    @Override
    public void handleRequest(PurchaseRequest request) {
        if (request.getAmount() <= VICE_PRESIDENT_LIMIT) {
            System.out.println("Vice President approves the purchase request of $" + request.getAmount());
        } else {
            System.out.println("Request exceeds the limit. No one can handle it.");
        }
    }

}
