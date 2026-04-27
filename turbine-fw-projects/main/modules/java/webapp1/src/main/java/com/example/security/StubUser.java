package com.example.security;

import org.apache.fulcrum.security.model.turbine.entity.impl.TurbineUserImpl;

public class StubUser extends TurbineUserImpl {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StubUser(String username) {
        setName(username);
        setPassword("stub"); // optional
       
    }
}