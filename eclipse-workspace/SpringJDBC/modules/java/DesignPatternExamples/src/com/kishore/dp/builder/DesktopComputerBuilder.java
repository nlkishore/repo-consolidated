package com.kishore.dp.builder;

public class DesktopComputerBuilder implements ComputerBuilder {

	private Computer computer;

    public DesktopComputerBuilder() {
        this.computer = new Computer("Default CPU", "Default Memory", "Default Storage", "Default Graphics Card");
    }

    @Override
    public ComputerBuilder setCpu(String cpu) {
       // computer.cpu = cpu;
    	computer.setCpu(cpu);
        return this;
    }

    @Override
    public ComputerBuilder setMemory(String memory) {
      //  computer.memory = memory;
    	computer.setMemory(memory);
        return this;
    }

    @Override
    public ComputerBuilder setStorage(String storage) {
       // computer.storage = storage;
    	computer.setMemory(storage);
        return this;
    }

    @Override
    public ComputerBuilder setGraphicsCard(String graphicsCard) {
       // computer.graphicsCard = graphicsCard;
    	computer.setGraphicsCard(graphicsCard);
        return this;
    }

    @Override
    public Computer build() {
        return computer;
    }

}
