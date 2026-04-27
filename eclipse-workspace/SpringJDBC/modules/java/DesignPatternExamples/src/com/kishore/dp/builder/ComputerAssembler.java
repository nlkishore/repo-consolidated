package com.kishore.dp.builder;

public class ComputerAssembler {
	private ComputerBuilder computerBuilder;

    public ComputerAssembler(ComputerBuilder computerBuilder) {
        this.computerBuilder = computerBuilder;
    }

    public Computer assembleComputer() {
        return computerBuilder.build();
    }
    
    public ComputerBuilder getComputerBuilder() {
    	return computerBuilder;
    }
}
