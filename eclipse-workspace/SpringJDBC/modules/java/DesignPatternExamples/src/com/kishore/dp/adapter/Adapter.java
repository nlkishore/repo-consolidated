package com.kishore.dp.adapter;

public class Adapter implements NewSystem {

	private OldSystem oldSystem;

    public Adapter(OldSystem oldSystem) {
        this.oldSystem = oldSystem;
    }

    @Override
    public void specificRequest() {
        // Delegate the request to the existing OldSystem
        oldSystem.request();
    }

}
