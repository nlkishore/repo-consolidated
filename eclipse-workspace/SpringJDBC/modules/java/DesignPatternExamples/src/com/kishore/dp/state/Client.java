package com.kishore.dp.state;

public class Client {
	public static void main(String[] args) {
        // Create an instance of the VendingMachine
        VendingMachine vendingMachine = new VendingMachine();

        // Perform actions based on the current state
        vendingMachine.insertCoin();
        vendingMachine.selectProduct();
        vendingMachine.ejectCoin();
        vendingMachine.selectProduct();
        vendingMachine.insertCoin();
        vendingMachine.dispenseProduct();
        vendingMachine.selectProduct();
    }

}
