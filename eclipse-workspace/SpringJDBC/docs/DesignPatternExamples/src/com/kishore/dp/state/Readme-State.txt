java Example Using State pattern
ChatGPT
The State Pattern is a behavioral design pattern that allows an object to alter its behavior when its internal state changes. This pattern is particularly useful when an object transitions through different states and its behavior needs to change accordingly. Let's consider a simple example of the State Pattern in Java, where we model the behavior of a vending machine.

java
Copy code
// Context: VendingMachine
class VendingMachine {
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

// State interface
interface VendingMachineState {
    void insertCoin();
    void ejectCoin();
    void selectProduct();
    void dispenseProduct();
}

// Concrete State 1: NoCoinState
class NoCoinState implements VendingMachineState {
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

// Concrete State 2: HasCoinState
class HasCoinState implements VendingMachineState {
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

// Concrete State 3: DispensingState
class DispensingState implements VendingMachineState {
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

// Client code
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
In this example:

VendingMachine is the context class that maintains the current state and delegates actions to the current state.
VendingMachineState is the state interface with methods representing the actions that can be performed in different states.
NoCoinState, HasCoinState, and DispensingState are concrete state classes implementing the VendingMachineState interface. Each class represents a different state of the vending machine and defines how the actions should be handled in that state.
The client code demonstrates the transitions between different states by performing actions such as inserting coins, selecting products, and ejecting coins.
The State Pattern allows the vending machine to change its behavior dynamically as it transitions through different states. It promotes encapsulation of state-specific behavior and makes it easier to add new states or modify existing ones without affecting the context class.