package com.kishore.dp.builder;

public interface ComputerBuilder {
	ComputerBuilder setCpu(String cpu);
    ComputerBuilder setMemory(String memory);
    ComputerBuilder setStorage(String storage);
    ComputerBuilder setGraphicsCard(String graphicsCard);
    Computer build();
}
