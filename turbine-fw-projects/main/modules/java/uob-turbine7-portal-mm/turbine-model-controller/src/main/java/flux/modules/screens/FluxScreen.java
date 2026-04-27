package com.uob.flux.modules.screens;

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

import org.apache.commons.configuration2.Configuration;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.annotation.TurbineConfiguration;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

/**
 * Base screen for secure web acces to the storage side of Tambora.
 *
 */
public abstract class FluxScreen extends VelocitySecureScreen {

	@TurbineService
	protected SecurityService securityService;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_LOGIN)
	private Configuration templateLogin;

	@TurbineConfiguration(TurbineConstants.TEMPLATE_HOMEPAGE)
	private Configuration templateHomepage;

	/**
	 * This method is called by Turbine
	 */
	@Override
	protected void doBuildTemplate(PipelineData data, Context context) throws Exception {

		/*
		 * Check to see if the embedded menu should be displayed in the templates.
		 */
		if (Turbine.getConfiguration().getBoolean("flux.embedded.show.menu", false)) {
			context.put("showEmbeddedMenu", true);
		}

		/*
		 * Check to see if we will display the finders on the forms used in Flux.
		 */
		if (Turbine.getConfiguration().getBoolean("flux.ui.show.finder", false)) {
			context.put("showFinder", true);
		}

	}

	/**
	 * Get SecurityService instance (same approach as UserProfile)
	 */
	private SecurityService getSecurityService() {
		if (securityService != null) {
			return securityService;
		}
		return (SecurityService) org.apache.turbine.services.TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);
	}

	@Override
	protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
		boolean isAuthorized = false;
		RunData data = (RunData) pipelineData;
		
		// Debug logging - MUST be at the very start
		org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());
		log.info("========================================");
		log.info("FluxScreen.isAuthorized() CALLED!");
		log.info("========================================");
		log.info("FluxScreen.isAuthorized() - Screen template: " + data.getScreenTemplate());
		log.info("FluxScreen.isAuthorized() - Screen: " + data.getScreen());
		if (data.getUser() != null) {
			log.info("FluxScreen.isAuthorized() - User: " + data.getUser().getName());
		} else {
			log.warn("FluxScreen.isAuthorized() - User is NULL!");
		}
		
		/*
		 * Grab the Flux Admin role listed in the Flux.properties file that is included
		 * in the the standard TurbineResources.properties file.
		 */
		String fluxAdminRole = Turbine.getConfiguration().getString("flux.admin.role");
		log.info("FluxScreen.isAuthorized() - fluxAdminRole: " + fluxAdminRole);

		// Get the Turbine ACL implementation
		// Try data.getACL() first (Menu.vm uses this and it works)
		TurbineAccessControlList acl = data.getACL();
		log.info("FluxScreen.isAuthorized() - ACL from data.getACL(): " + (acl != null ? "not null" : "NULL"));
		
		if (acl != null) {
			org.apache.fulcrum.security.util.RoleSet roles = acl.getRoles();
			log.info("FluxScreen.isAuthorized() - ACL from data.getACL() contains " + (roles != null ? roles.size() : 0) + " role(s)");
			if (roles != null && roles.size() > 0) {
				log.info("FluxScreen.isAuthorized() - Roles from data.getACL():");
				for (org.apache.fulcrum.security.entity.Role role : roles) {
					log.info("  - Role: '" + role.getName() + "' (ID: " + role.getId() + ")");
				}
			}
		}
		
		// If data.getACL() has no roles, try getUserManager().getACL() as fallback
		if (acl == null || (acl.getRoles() != null && acl.getRoles().size() == 0)) {
			log.warn("FluxScreen.isAuthorized() - data.getACL() has no roles, trying getUserManager().getACL()");
			org.apache.turbine.om.security.User user = data.getUser();
			SecurityService security = getSecurityService();
			if (user != null && security != null) {
				try {
					log.info("FluxScreen.isAuthorized() - Getting ACL via security.getUserManager().getACL(user)");
					@SuppressWarnings("unchecked")
					TurbineAccessControlList aclFromManager = (TurbineAccessControlList) security.getUserManager().getACL(user);
					if (aclFromManager != null) {
						org.apache.fulcrum.security.util.RoleSet roles = aclFromManager.getRoles();
						log.info("FluxScreen.isAuthorized() - ACL from getUserManager() contains " + (roles != null ? roles.size() : 0) + " role(s)");
						if (roles != null && roles.size() > 0) {
							acl = aclFromManager; // Use this ACL if it has roles
							log.info("FluxScreen.isAuthorized() - Using ACL from getUserManager()");
						}
					}
				} catch (Exception e) {
					log.error("FluxScreen.isAuthorized() - Error getting ACL via getUserManager(): " + e.getMessage(), e);
				}
			}
		}
		
		log.info("FluxScreen.isAuthorized() - ACL is null: " + (acl == null));
		
		// UNIQUE MARKER - If you see this, the new code is running!
		log.error("*** FLUXSCREEN DEBUG MARKER - NEW CODE VERSION 2026-01-25-0730 ***");

		if (acl == null) {
			// commons configuration getProperty: prefix removed, the key for the value ..
			// is an empty string, the result an object
			log.warn("FluxScreen.isAuthorized() - ACL is null, denying access");
			data.setScreenTemplate((String) templateLogin.getProperty(""));
			isAuthorized = false;
		} else {
			log.info("========================================");
			log.info("FluxScreen.isAuthorized() - Starting role checks...");
			log.info("========================================");
			
			// Debug: Get all roles from ACL to see what's actually available
			org.apache.fulcrum.security.util.RoleSet allRoles = null;
			try {
				log.info("FluxScreen.isAuthorized() - Calling acl.getRoles()...");
				allRoles = acl.getRoles();
				log.info("FluxScreen.isAuthorized() - ACL contains " + (allRoles != null ? allRoles.size() : 0) + " role(s)");
				if (allRoles != null && allRoles.size() > 0) {
					log.info("FluxScreen.isAuthorized() - Available roles:");
					for (org.apache.fulcrum.security.entity.Role role : allRoles) {
						log.info("  - Role: '" + role.getName() + "' (ID: " + role.getId() + ")");
					}
				} else {
					log.warn("FluxScreen.isAuthorized() - ACL.getRoles() returned empty or null");
				}
			} catch (Exception e) {
				log.error("FluxScreen.isAuthorized() - Error getting roles from ACL: " + e.getMessage(), e);
			}
			
			// Check all role variations (case-sensitive)
			log.info("FluxScreen.isAuthorized() - Checking hasRole() for various role names...");
			boolean hasTurbineAdmin = acl.hasRole(fluxAdminRole);
			boolean hasAdmin = acl.hasRole("admin");
			boolean hasADMIN = acl.hasRole("ADMIN");
			boolean hasAdminMixed = acl.hasRole("Admin");
			
			log.info("FluxScreen.isAuthorized() - hasRole('" + fluxAdminRole + "'): " + hasTurbineAdmin);
			log.info("FluxScreen.isAuthorized() - hasRole('admin'): " + hasAdmin);
			log.info("FluxScreen.isAuthorized() - hasRole('ADMIN'): " + hasADMIN);
			log.info("FluxScreen.isAuthorized() - hasRole('Admin'): " + hasAdminMixed);
			
			// Also check by iterating through roles manually with case-insensitive and trimmed comparison
			boolean foundAdminRole = false;
			String foundRoleName = null;
			try {
				log.info("FluxScreen.isAuthorized() - Iterating through roles manually (case-insensitive, trimmed)...");
				if (allRoles != null) {
					for (org.apache.fulcrum.security.entity.Role role : allRoles) {
						String roleName = role.getName();
						String roleNameTrimmed = roleName != null ? roleName.trim() : "";
						String roleNameLower = roleNameTrimmed.toLowerCase();
						
						// Log role details including length and hex for debugging spaces
						log.info("FluxScreen.isAuthorized() - Checking role: '" + roleName + "' (length: " + 
							(roleName != null ? roleName.length() : 0) + ", trimmed: '" + roleNameTrimmed + "')");
						
						// Check against all variations (case-insensitive, trimmed)
						if ("turbineadmin".equalsIgnoreCase(roleNameTrimmed) || 
						    "admin".equalsIgnoreCase(roleNameTrimmed) || 
						    "ADMIN".equalsIgnoreCase(roleNameTrimmed) ||
						    roleNameLower.equals("admin") ||
						    roleNameLower.equals("turbineadmin")) {
							foundAdminRole = true;
							foundRoleName = roleName;
							log.info("FluxScreen.isAuthorized() - Found admin role via iteration: '" + roleName + "' (original), '" + roleNameTrimmed + "' (trimmed)");
							break;
						}
					}
				} else {
					log.warn("FluxScreen.isAuthorized() - Cannot iterate roles - allRoles is null");
				}
			} catch (Exception e) {
				log.error("FluxScreen.isAuthorized() - Error iterating roles: " + e.getMessage(), e);
			}
			
			// Also check if username contains "admin" as fallback (like Menu.vm does)
			boolean usernameHasAdmin = false;
			try {
				org.apache.turbine.om.security.User user = data.getUser();
				if (user != null && user.getName() != null) {
					String username = user.getName().toLowerCase();
					if (username.contains("admin")) {
						usernameHasAdmin = true;
						log.info("FluxScreen.isAuthorized() - Username contains 'admin': " + user.getName());
					}
				}
			} catch (Exception e) {
				log.warn("FluxScreen.isAuthorized() - Error checking username: " + e.getMessage());
			}
			
			if (hasTurbineAdmin || hasAdmin || hasADMIN || hasAdminMixed || foundAdminRole || usernameHasAdmin) {
				// Check for turbineadmin role (Flux default) or ADMIN role variations
				log.info("FluxScreen.isAuthorized() - Access GRANTED");
				if (foundAdminRole) {
					log.info("FluxScreen.isAuthorized() - Granted via role iteration: '" + foundRoleName + "'");
				} else if (usernameHasAdmin) {
					log.info("FluxScreen.isAuthorized() - Granted via username check (contains 'admin')");
				} else {
					log.info("FluxScreen.isAuthorized() - Granted via hasRole() check");
				}
				isAuthorized = true;
			} else {
				log.warn("FluxScreen.isAuthorized() - Access DENIED - No matching admin role found");
				log.warn("FluxScreen.isAuthorized() - hasRole checks: turbineadmin=" + hasTurbineAdmin + 
					", admin=" + hasAdmin + ", ADMIN=" + hasADMIN + ", Admin=" + hasAdminMixed);
				log.warn("FluxScreen.isAuthorized() - foundAdminRole=" + foundAdminRole + ", usernameHasAdmin=" + usernameHasAdmin);
				data.setScreenTemplate((String) templateHomepage.getProperty(""));
				data.setMessage("You do not have access to this part of the site.");
				isAuthorized = false;
			}
		}
		return isAuthorized;
	}
}
