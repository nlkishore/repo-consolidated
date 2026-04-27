package com.kishore.dp.command;

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
