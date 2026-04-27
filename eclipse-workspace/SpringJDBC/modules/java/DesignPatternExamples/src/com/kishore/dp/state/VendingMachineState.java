package com.kishore.dp.state;

//State interface
interface VendingMachineState {
 void insertCoin();
 void ejectCoin();
 void selectProduct();
 void dispenseProduct();
}
