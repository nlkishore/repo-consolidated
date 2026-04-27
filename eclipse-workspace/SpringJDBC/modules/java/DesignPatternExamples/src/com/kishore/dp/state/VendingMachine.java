package com.kishore.dp.state;

public class VendingMachine {
	
	private VendingMachineState state;

    public VendingMachine() {
        // Initial state is NoCoinState
        this.state = new NoCoinState(this);
    }

    public void setState(VendingMachineState state) {
        this.state = state;
    }

    public void insertCoin() {
        state.insertCoin();
    }

    public void ejectCoin() {
        state.ejectCoin();
    }

    public void selectProduct() {
        state.selectProduct();
    }

    public void dispenseProduct() {
        state.dispenseProduct();
    }

}
