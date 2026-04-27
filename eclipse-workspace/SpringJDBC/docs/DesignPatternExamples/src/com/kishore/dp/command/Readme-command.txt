Java Example  using command pattern
ChatGPT
The Command Pattern is a behavioral design pattern that encapsulates a request as an object, allowing users to parameterize clients with queues, requests, and operations. It also allows users to support undoable operations. Let's consider a simple example of the Command Pattern in Java where we have a remote control that can be used to turn on and off devices.

java
Copy code
// Command interface
interface Command {
    void execute();
}

// Concrete Command 1: TurnOnCommand
class TurnOnCommand implements Command {
    private Device device;

    public TurnOnCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        device.turnOn();
    }
}

// Concrete Command 2: TurnOffCommand
class TurnOffCommand implements Command {
    private Device device;

    public TurnOffCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        device.turnOff();
    }
}

// Receiver: Device
class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }

    public void turnOn() {
        System.out.println(name + " is turned on.");
    }

    public void turnOff() {
        System.out.println(name + " is turned off.");
    }
}

// Invoker: RemoteControl
class RemoteControl {
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }
}

// Client code
public class Client {
    public static void main(String[] args) {
        // Create instances of devices
        Device light = new Device("Light");
        Device fan = new Device("Fan");

        // Create instances of commands
        Command turnOnLight = new TurnOnCommand(light);
        Command turnOffLight = new TurnOffCommand(light);
        Command turnOnFan = new TurnOnCommand(fan);
        Command turnOffFan = new TurnOffCommand(fan);

        // Create an instance of the invoker (RemoteControl)
        RemoteControl remoteControl = new RemoteControl();

        // Set commands for the remote control
        remoteControl.setCommand(turnOnLight);

        // Press the button to turn on the light
        remoteControl.pressButton();

        // Set a different command for the remote control
        remoteControl.setCommand(turnOffFan);

        // Press the button to turn off the fan
        remoteControl.pressButton();
    }
}
In this example:

Command is the command interface with the execute method.
TurnOnCommand and TurnOffCommand are concrete command classes that implement the Command interface and encapsulate the operations to turn on and off a device.
Device is the receiver class that performs the actual operations (turning on and off).
RemoteControl is the invoker class that sets and triggers commands.
The client code demonstrates how to use the Command Pattern by creating instances of devices, commands, and the invoker. The invoker can be easily configured with different commands, providing flexibility and decoupling between the sender (client) and the receiver (device).