package com.example.om;

import java.util.Date;
import java.util.Map;

import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.turbine.om.security.User;

public class GtpUser
    extends BaseGtpUser
    implements TurbineUser, User {

    private static final long serialVersionUID = 1758967634016L;

    // Tracks login state for Turbine rendering
    private boolean loggedIn = true;

    @Override
    public boolean hasLoggedIn() {
        return loggedIn;
    }

	
	  public void setHasLoggedIn(boolean value) { this.loggedIn = value;
	  }
	

    // Optional: override getName() if needed for display
    @Override
    public String getName() {
        return getFirstName(); // or getUsername(), depending on your schema
    }

    // Optional: override getEmail() if required by templates
    @Override
    public String getEmail() {
        return super.getEmail(); // assuming BaseGtpUser has getEmail()
    }

	@Override
	public TurbineUser getUserDelegate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserDelegate(TurbineUser userDelegate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getAccessCounter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAccessCounterForSession() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getLastAccessDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPerm(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPerm(String name, Object def) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getPermStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getTempStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getTemp(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getTemp(String name, Object def) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHasLoggedIn(Boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void incrementAccessCounter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void incrementAccessCounterForSession() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object removeTemp(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAccessCounter(int cnt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAccessCounterForSession(int cnt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLastAccessDate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPerm(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPermStorage(Map<String, Object> storage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTempStorage(Map<String, Object> storage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTemp(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConfirmed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateLastLogin() throws Exception {
		// TODO Auto-generated method stub
		
	}

    // You can add more overrides here if Turbine expects them
}