package com.uob.flux.modules.actions.user;

/*
 * Copyright 2001-2019 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.uob.flux.modules.actions.FluxAction;

/**
 * Change user action
 */
public class FluxUserAction extends FluxAction 
{
	/** Logging **/
	private static Log log = LogFactory.getLog(FluxUserAction.class);

	/** Injected service instance */
	@TurbineService
	private SecurityService security;

	/**
	 * ActionEvent responsible for inserting a new user into the Turbine security
	 * system.
	 */
	public void doInsert(PipelineData pipelineData, Context context) throws Exception 
	{
		log.info("========================================");
		log.info("FluxUserAction.doInsert() CALLED!");
		log.info("========================================");
		
		RunData data = (RunData) pipelineData;

		/*
		 * Grab the username entered in the form.
		 */
		String username = data.getParameters().getString("username");
		String password = data.getParameters().getString("password");
		
		log.info("FluxUserAction.doInsert() - Username: " + (username != null ? username : "null"));
		log.info("FluxUserAction.doInsert() - Password: " + (password != null && !password.isEmpty() ? "***" : "null/empty"));

		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) 
		{
			/*
			 * Make sure this account doesn't already exist. If the account already exists
			 * then alert the user and make them change the username.
			 */
			if (security.accountExists(username)) 
			{
				context.put("username", username);
				context.put("errorTemplate", "user,FluxUserAlreadyExists.vm");

				data.setMessage("The user already exists");
				data.getParameters().add("mode", "insert");
				data.setScreen("user,FluxUserForm.vm");
				return;
			} 
			else 
			{

				try 
				{
					/*
					 * Create a new user modeled directly from the SecurityServiceTest method
					 */
					log.info("FluxUserAction.doInsert() - Creating new user: " + username);
					User user = security.getUserInstance(username);
					data.getParameters().setProperties(user);
					
					log.info("FluxUserAction.doInsert() - User instance created, calling security.addUser()");
					security.addUser(user, password);
					log.info("FluxUserAction.doInsert() - User added successfully, user ID: " + user.getId());

					// Use security to force the password
					security.forcePassword(user, password);
					log.info("FluxUserAction.doInsert() - Password set for user: " + username);

					// Redirect to user list after successful creation
					data.setMessage("User '" + username + "' created successfully");
					data.setScreen("user,FluxUserList.vm");
					log.info("FluxUserAction.doInsert() - Redirecting to user list");
					return;

				} 
				catch (Exception e) 
				{
					log.error("FluxUserAction.doInsert() - Error adding new user: " + username, e);

					context.put("username", username);
					context.put("errorTemplate", "user,FluxUserAlreadyExists.vm");

					data.setMessage("Could not add the user: " + e.getMessage());
					data.getParameters().add("mode", "insert");
					data.setScreen("user,FluxUserForm.vm");
					return;
				}
			}

		} else {
			String msg = "Cannot add user without username or password";
			log.error("FluxUserAction.doInsert() - " + msg);
			log.error("FluxUserAction.doInsert() - Username empty: " + (username == null || username.isEmpty()));
			log.error("FluxUserAction.doInsert() - Password empty: " + (password == null || password.isEmpty()));
			data.setMessage(msg);
			data.getParameters().add("mode", "insert");
			data.setScreen("user,FluxUserForm.vm");
		}
		
		log.info("FluxUserAction.doInsert() - Method completed");
	}

	/**
	 * ActionEvent responsible updating a user
	 */
	public void doUpdate(PipelineData pipelineData, Context context) throws Exception 
	{
		RunData data = (RunData) pipelineData;
		String username = data.getParameters().getString("username");
		if (!StringUtils.isEmpty(username)) 
		{
			if (security.accountExists(username)) 
			{
				// Load the wrapped user object
				User user = security.getUser(username);
				User tmp_user = security.getUser(username);
				if (user != null) {

					// Update user details except for the password
					data.getParameters().setProperties(user);
					user.setPassword(tmp_user.getPassword());
					security.saveUser(user);

					// Test if Admin provided new password
					String password = data.getParameters().getString("password");
					if (!StringUtils.isEmpty(password)) 
					{
						// Change user password
						security.changePassword(user, user.getPassword(), password);
						security.forcePassword(user, password);
					} else {
						data.setMessage("Cannot provide an empty password");
						return;
					}

				}

			} else {
				log.error("User does not exist!");
			}
		}
	}

	/**
	 * ActionEvent responsible for removing a user
	 */
	public void doDelete(PipelineData pipelineData, Context context) throws Exception 
	{

		try 
		{
			RunData data = (RunData) pipelineData;
			String username = data.getParameters().getString("username");
			if (!StringUtils.isEmpty(username)) 
			{
				if (security.accountExists(username)) 
				{
					// find the user object and remove using security mgr
					User user = security.getUser(username);

					// get the turbine user id
					int id = (int) user.getId();

					// remove the turbine user
					security.removeUser(user);

				} else {
					log.error("User does not exist!");
					data.setMessage("User not found!");
				}
			}
		} 
		catch (Exception e) 
		{
			log.error("Could not remove user: " + e);
		}
	}

	/**
	 * Update the roles that are to assigned to a user for a project.
	 */
	public void doRoles(PipelineData pipelineData, Context context) throws Exception 
	{
		log.info("========================================");
		log.info("FluxUserAction.doRoles() CALLED!");
		log.info("========================================");
		
		RunData data = (RunData) pipelineData;

		try 
		{
			/*
			 * Get the user we are trying to update. The username has been hidden in the
			 * form so we will grab the hidden username and use that to retrieve the user.
			 */
			String username = data.getParameters().getString("username");
			log.info("FluxUserAction.doRoles() - Username: " + (username != null ? username : "null"));
			
			if (!StringUtils.isEmpty(username)) 
			{
				if (security.accountExists(username)) 
				{
					User user = security.getUser(username);
					log.info("FluxUserAction.doRoles() - User found: " + user.getName() + " (ID: " + user.getId() + ")");

					// Get the Turbine ACL implementation
					TurbineAccessControlList acl = (TurbineAccessControlList) security.getUserManager().getACL(user);
					log.info("FluxUserAction.doRoles() - ACL retrieved: " + (acl != null ? "Yes" : "No"));

					/*
					 * Grab all the Groups and Roles in the system.
					 */
					GroupSet groups = security.getAllGroups();
					RoleSet roles = security.getAllRoles();
					log.info("FluxUserAction.doRoles() - Total groups: " + (groups != null ? groups.size() : 0) + 
					         ", Total roles: " + (roles != null ? roles.size() : 0));

					int grantedCount = 0;
					int revokedCount = 0;
					int skippedCount = 0;
					int errorCount = 0;

					for (Group group : groups) 
					{
						String groupName = group.getName();
						for (Role role : roles) 
						{
							String roleName = role.getName();

							/*
							 * In the UserAssignment.vm we made a checkbox for every possible Group/Role
							 * combination so we will compare every possible combination with the values
							 * that were checked off in the form. If we have a match then we will grant the
							 * user the role in the group.
							 */
							String groupRole = groupName + roleName;
							String formGroupRole = data.getParameters().getString(groupRole);
							boolean addGroupRole = !StringUtils.isEmpty(formGroupRole) && 
							                      ("true".equalsIgnoreCase(formGroupRole) || formGroupRole.length() > 0);

							if (addGroupRole) {
								// Check if role already assigned (using direct DB query)
								boolean alreadyHasRole = false;
								try {
									Integer userId = Integer.valueOf(user.getId().toString());
									Integer groupId = Integer.valueOf(group.getId().toString());
									Integer roleId = Integer.valueOf(role.getId().toString());
									
									org.apache.torque.criteria.Criteria checkCriteria = new org.apache.torque.criteria.Criteria();
									checkCriteria.where(com.uob.om.GtpUserGroupRolePeer.USER_ID, userId);
									checkCriteria.and(com.uob.om.GtpUserGroupRolePeer.GROUP_ID, groupId);
									checkCriteria.and(com.uob.om.GtpUserGroupRolePeer.ROLE_ID, roleId);
									
									List<com.uob.om.GtpUserGroupRole> existing = 
										com.uob.om.GtpUserGroupRolePeer.doSelect(checkCriteria);
									alreadyHasRole = (existing != null && !existing.isEmpty());
								} catch (Exception e) {
									log.warn("FluxUserAction.doRoles() - Error checking existing assignment: " + e.getMessage());
								}
								
								if (!alreadyHasRole) {
									try {
										log.info("FluxUserAction.doRoles() - Granting role '" + roleName + 
										         "' to user '" + username + "' in group '" + groupName + "'");
										
										// Direct insert into GTP_USER_GROUP_ROLE table (bypass framework)
										Integer userId = Integer.valueOf(user.getId().toString());
										Integer groupId = Integer.valueOf(group.getId().toString());
										Integer roleId = Integer.valueOf(role.getId().toString());
										
										com.uob.om.GtpUserGroupRole ugr = new com.uob.om.GtpUserGroupRole();
										ugr.setUserId(userId);
										ugr.setGroupId(groupId);
										ugr.setRoleId(roleId);
										ugr.save();
										
										grantedCount++;
										log.info("FluxUserAction.doRoles() - Successfully granted role (direct DB insert)");
									} catch (Exception e) {
										errorCount++;
										log.error("FluxUserAction.doRoles() - Error granting role '" + roleName + 
										          "' to user '" + username + "' in group '" + groupName + "': " + 
										          e.getMessage(), e);
									}
								} else {
									skippedCount++;
									log.debug("FluxUserAction.doRoles() - User already has role '" + roleName + 
									          "' in group '" + groupName + "', skipping");
								}

							} else {
								// Remove role if it was previously assigned (using direct DB query)
								boolean hasRole = false;
								try {
									Integer userId = Integer.valueOf(user.getId().toString());
									Integer groupId = Integer.valueOf(group.getId().toString());
									Integer roleId = Integer.valueOf(role.getId().toString());
									
									org.apache.torque.criteria.Criteria checkCriteria = new org.apache.torque.criteria.Criteria();
									checkCriteria.where(com.uob.om.GtpUserGroupRolePeer.USER_ID, userId);
									checkCriteria.and(com.uob.om.GtpUserGroupRolePeer.GROUP_ID, groupId);
									checkCriteria.and(com.uob.om.GtpUserGroupRolePeer.ROLE_ID, roleId);
									
									List<com.uob.om.GtpUserGroupRole> existing = 
										com.uob.om.GtpUserGroupRolePeer.doSelect(checkCriteria);
									hasRole = (existing != null && !existing.isEmpty());
								} catch (Exception e) {
									log.warn("FluxUserAction.doRoles() - Error checking existing assignment for removal: " + e.getMessage());
								}
								
								if (hasRole) {
									try {
										log.info("FluxUserAction.doRoles() - Revoking role '" + roleName + 
										         "' from user '" + username + "' in group '" + groupName + "'");
										
										// Direct delete from GTP_USER_GROUP_ROLE table (bypass framework)
										Integer userId = Integer.valueOf(user.getId().toString());
										Integer groupId = Integer.valueOf(group.getId().toString());
										Integer roleId = Integer.valueOf(role.getId().toString());
										
										org.apache.torque.criteria.Criteria deleteCriteria = new org.apache.torque.criteria.Criteria();
										deleteCriteria.where(com.uob.om.GtpUserGroupRolePeer.USER_ID, userId);
										deleteCriteria.and(com.uob.om.GtpUserGroupRolePeer.GROUP_ID, groupId);
										deleteCriteria.and(com.uob.om.GtpUserGroupRolePeer.ROLE_ID, roleId);
										
										com.uob.om.GtpUserGroupRolePeer.doDelete(deleteCriteria);
										
										revokedCount++;
										log.info("FluxUserAction.doRoles() - Successfully revoked role (direct DB delete)");
									} catch (Exception e) {
										errorCount++;
										log.error("FluxUserAction.doRoles() - Error revoking role '" + roleName + 
										          "' from user '" + username + "' in group '" + groupName + "': " + 
										          e.getMessage(), e);
									}
								}
							}
						}
					}

					log.info("FluxUserAction.doRoles() - Summary: Granted=" + grantedCount + 
					         ", Revoked=" + revokedCount + ", Skipped=" + skippedCount + ", Errors=" + errorCount);

					// Redirect back to UserAssignment with selected user to show updated assignments
					data.setMessage("Assignments updated successfully. Granted: " + grantedCount + 
					                ", Revoked: " + revokedCount + 
					                (errorCount > 0 ? ", Errors: " + errorCount : ""));
					data.getParameters().add("selectedUser", username);
					// Use setScreenTemplate() with template path
					data.setScreenTemplate("UserAssignment.vm");
					log.info("FluxUserAction.doRoles() - Redirecting to UserAssignment with selectedUser=" + username);

				} else {
					log.error("FluxUserAction.doRoles() - User does not exist: " + username);
					data.setMessage("User '" + username + "' not found");
					data.setScreenTemplate("UserAssignment.vm");
				}
			} else {
				log.error("FluxUserAction.doRoles() - Username is empty");
				data.setMessage("Username is required");
				data.setScreenTemplate("UserAssignment.vm");
			}

		} catch (Exception e) {
			log.error("FluxUserAction.doRoles() - Error on role assignment: " + e.getMessage(), e);
			data.setMessage("Error updating assignments: " + e.getMessage());
			data.setScreenTemplate("UserAssignment.vm");
		}
		
		log.info("FluxUserAction.doRoles() - Method completed");
	}

	/**
	 * Implement this to add information to the context.
	 */
	public void doPerform(PipelineData pipelineData, Context context) throws Exception 
	{
		log.info("Running do perform!");
		( (RunData) pipelineData).setMessage("Can't find the requested action!");
	}

}
