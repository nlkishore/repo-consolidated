package com.kishore.dp.state;

public class DispensingState implements VendingMachineState {

	private VendingMachine vendingMachine;

    public DispensingState(VendingMachine vendingMachine) {
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void insertCoin() {
        System.out.println("Cannot insert coin while dispensing");
    }

    @Override
    public void ejectCoin() {
        System.out.println("Cannot eject coin while dispensing");
    }

    @Override
    public void selectProduct() {
        System.out.println("Cannot select another product while dispensing");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Product dispensed");
        vendingMachine.setState(new NoCoinState(vendingMachine));
    }

}
