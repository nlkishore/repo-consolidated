package com.example.security;

import org.apache.fulcrum.security.model.turbine.TurbineUserManager;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.PasswordMismatchException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.fulcrum.security.util.UserSet;

import com.example.om.GtpUser;

import java.util.Collections;
import java.util.List;

import org.apache.fulcrum.security.acl.AccessControlList;
import org.apache.fulcrum.security.entity.User;

public class StubUserManager implements TurbineUserManager {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public User getUser(String username) {
        return new StubUser(username);
    }

      


	@Override
	public <T extends User> T getUserInstance() throws DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends User> T getUserInstance(String userName) throws DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkExists(User user) throws DataBackendException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkExists(String userName) throws DataBackendException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends User> T getUserById(Object id) throws UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends User> T getUser(String username, String password)
			throws PasswordMismatchException, UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserSet<GtpUser> getAllUsers() {
	    return new UserSet<>(); // if constructor is public
	}


	@Override
	public <T extends User> UserSet<T> retrieveUserList(Object criteria) throws DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveUser(User user) throws UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void authenticate(User user, String password)
			throws PasswordMismatchException, UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends User> T addUser(T user, String password) throws EntityExistsException, DataBackendException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeUser(User user) throws UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changePassword(User user, String oldPassword, String newPassword)
			throws PasswordMismatchException, UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forcePassword(User user, String password) throws UnknownEntityException, DataBackendException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends AccessControlList> T getACL(User user) throws UnknownEntityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends User> T getAnonymousUser() throws UnknownEntityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAnonymousUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

    // Implement other required methods with safe stubs or throw UnsupportedOperationException
}