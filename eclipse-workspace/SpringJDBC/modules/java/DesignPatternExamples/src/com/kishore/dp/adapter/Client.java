package com.kishore.dp.adapter;

public class Client {
	public static void main(String[] args) {
        // Using the New System directly
        NewSystem newSystem = new NewSystemImpl();
        newSystem.specificRequest();

        System.out.println("-----------------------------");

        // Using the Old System through the Adapter
        OldSystem oldSystem = new OldSystemImpl();
        NewSystem adaptedSystem = new Adapter(oldSystem);
        adaptedSystem.specificRequest();
    }

}
