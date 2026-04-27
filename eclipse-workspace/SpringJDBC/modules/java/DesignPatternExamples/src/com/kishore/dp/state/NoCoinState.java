package com.kishore.dp.state;

public class NoCoinState implements VendingMachineState {

	private VendingMachine vendingMachine;

    public NoCoinState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void insertCoin() {
        System.out.println("Coin inserted");
        vendingMachine.setState(new HasCoinState(vendingMachine));
    }

    @Override
    public void ejectCoin() {
        System.out.println("No coin to eject");
    }

    @Override
    public void selectProduct() {
        System.out.println("Please insert a coin first");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please insert a coin and select a product");
    }

}
