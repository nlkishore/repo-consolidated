package com.example.wrapper;

import com.example.om.GtpUser;
import org.apache.turbine.om.security.User;

public class GtpUserWrapper extends GtpUser implements User {

    private static final long serialVersionUID = 1L;

    // Tracks login state for Turbine rendering
    private boolean loggedIn = false;

    public GtpUserWrapper(GtpUser user) {
    	super(); // initialize base class

        // Copy all relevant fields from the original user
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        this.setPassword(user.getPassword());
        this.setCreateDate(user.getCreateDate());
        this.setLastLogin(user.getLastLogin());
        this.setAccessCounter(user.getAccessCounter());
        this.setConfirmed(user.getConfirmed());

        // Copy any custom fields from your schema
        //this.setUsername(user.getName()g);
        this.setId(user.getId()); // if using primary key
        // Add more setters as needed based on your schema

		// TODO Auto-generated constructor stub
	}

	@Override
    public boolean hasLoggedIn() {
        return loggedIn;
    }

    @Override
    public void setHasLoggedIn(boolean value) {
        this.loggedIn = value;
    }

    @Override
    public String getName() {
        // Customize this based on your schema
        return getFirstName(); // or getUsername(), getFullName(), etc.
    }

    @Override
    public String getEmail() {
        return super.getEmail(); // assuming GtpUser has getEmail()
    }

    // Optional: override getPassword() if needed
    @Override
    public String getPassword() {
        return super.getPassword(); // or return null if not exposed
    }

    // Optional: override getConfirmed() if your schema supports it
	/*
	 * @Override public boolean getConfirmed() { return true; // or use a field if
	 * applicable }
	 */

    // Optional: override getCreateDate() if needed
    @Override
    public java.util.Date getCreateDate() {
        return super.getCreateDate(); // or return new Date() if not tracked
    }

    // Optional: override getLastLogin() if tracked
    @Override
    public java.util.Date getLastLogin() {
        return super.getLastLogin(); // or return null
    }

    // Optional: override getAccessCounter() if needed
    @Override
    public int getAccessCounter() {
        return 0; // or use a field if tracked
    }

    @Override
    public void incrementAccessCounter() {
        // implement if needed
    }
}