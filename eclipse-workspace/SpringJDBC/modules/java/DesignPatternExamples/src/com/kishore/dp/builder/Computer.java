package com.kishore.dp.builder;

public class Computer {
	private String cpu;
	private String memory;
    private String storage;
    private String graphicsCard;
    
    public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getGraphicsCard() {
		return graphicsCard;
	}

	public void setGraphicsCard(String graphicsCard) {
		this.graphicsCard = graphicsCard;
	}



    public Computer(String cpu, String memory, String storage, String graphicsCard) {
        this.cpu = cpu;
        this.memory = memory;
        this.storage = storage;
        this.graphicsCard = graphicsCard;
    }

    // Getter methods for the properties

    @Override
    public String toString() {
        return "Computer{" +
                "cpu='" + cpu + '\'' +
                ", memory='" + memory + '\'' +
                ", storage='" + storage + '\'' +
                ", graphicsCard='" + graphicsCard + '\'' +
                '}';
    }
}
