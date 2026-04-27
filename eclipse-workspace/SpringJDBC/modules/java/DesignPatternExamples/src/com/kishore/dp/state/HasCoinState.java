package com.kishore.dp.state;

public class HasCoinState implements VendingMachineState {

	private VendingMachine vendingMachine;

    public HasCoinState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void insertCoin() {
        System.out.println("Coin already inserted");
    }

    @Override
    public void ejectCoin() {
        System.out.println("Coin ejected");
        vendingMachine.setState(new NoCoinState(vendingMachine));
    }

    @Override
    public void selectProduct() {
        System.out.println("Product selected");
        vendingMachine.setState(new DispensingState(vendingMachine));
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please select a product first");
    }

}
