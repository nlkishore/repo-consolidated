package com.kishore.dp.command;

public class TurnOffCommand implements Command {

	private Device device;

    public TurnOffCommand(Device device) {
        this.device = device;
    }

    @Override
    public void execute() {
        device.turnOff();
    }

}
