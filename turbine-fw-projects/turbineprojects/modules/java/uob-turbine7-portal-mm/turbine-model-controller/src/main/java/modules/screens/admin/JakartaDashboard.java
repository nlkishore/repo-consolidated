package com.uob.modules.screens.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.torque.criteria.Criteria;

import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.PermissionSet;

import com.uob.modules.screens.SecureScreen;

/**
 * Jakarta UI Dashboard Screen - Admin Dashboard Content with Jakarta UI Layout
 * 
 * This is a Jakarta-styled admin dashboard that displays Admin Dashboard data
 * (Users, Groups, Roles, Permissions) with Jakarta UI styling and layout.
 * Access via: /app/admin/JakartaDashboard.vm
 * 
 * Features:
 * - Modern Jakarta UI styling
 * - Dropdown navigation menu (like reference UI)
 * - Admin Dashboard content (Users, Groups, Roles, Permissions)
 * - Search functionality
 * - Data grids
 */
public class JakartaDashboard extends SecureScreen
{
    /** Logging */
    private static Log log = LogFactory.getLog(JakartaDashboard.class);

    @TurbineService
    private SecurityService securityService;

    /**
     * Get SecurityService instance
     */
    private SecurityService getSecurityService() {
        if (securityService == null) {
            return (SecurityService) org.apache.turbine.services.TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);
        }
        return securityService;
    }

    @Override
    protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
        RunData data = (RunData) pipelineData;
        User user = data.getUser();
        
        log.info("JakartaDashboard.isAuthorized() called for user: " + (user != null ? user.getName() : "null"));
        
        if (user == null || !user.hasLoggedIn()) {
            log.warn("JakartaDashboard: User is not logged in - redirecting to login");
            data.setMessage("Please login to access Jakarta dashboard");
            data.setScreen("Login");
            return false;
        }
        
        // Check if user has admin role
        @SuppressWarnings("rawtypes")
        org.apache.fulcrum.security.model.turbine.TurbineAccessControlList acl = 
            (org.apache.fulcrum.security.model.turbine.TurbineAccessControlList) data.getACL();
        
        if (acl == null) {
            log.warn("JakartaDashboard: ACL is null - denying access");
            data.setMessage("You do not have permission to access Jakarta dashboard");
            return false;
        }
        
        // Check role-based authorization
        boolean isAdmin = acl.hasRole("turbineadmin") || 
                         acl.hasRole("admin") || 
                         acl.hasRole("ADMIN") || 
                         acl.hasRole("Admin");
        
        // Username fallback
        boolean usernameHasAdmin = false;
        if (user != null && user.getName() != null) {
            String username = user.getName().toLowerCase();
            if (username.contains("admin")) {
                usernameHasAdmin = true;
            }
        }
        
        if (isAdmin || usernameHasAdmin) {
            log.info("JakartaDashboard: User is authorized");
            return true;
        } else {
            log.warn("JakartaDashboard: User " + user.getName() + " does not have admin role - denying access");
            data.setMessage("You must be an Administrator to access Jakarta dashboard");
            return false;
        }
    }

    /**
     * This method is called by the Turbine framework when the screen is accessed.
     *
     * @param data the RunData object
     * @param context the Velocity context
     * @exception Exception a generic exception
     */
    @Override
    protected void doBuildTemplate(PipelineData data, Context context)
            throws Exception
    {
        log.info("JakartaDashboard.doBuildTemplate() called");
        
        RunData runData = (RunData) data;
        try
        {
            SecurityService security = getSecurityService();
            User user = runData.getUser();
            
            // Put user in context
            context.put("user", user);
            
            // Load all users (same as Admin Dashboard)
            List<User> allUsers = new ArrayList<>();
            try {
                Criteria criteria = new Criteria();
                @SuppressWarnings("unchecked")
                List<User> users = (List<User>) security.getUserManager().retrieveList(criteria);
                if (users != null) {
                    allUsers.addAll(users);
                    log.info("JakartaDashboard: Retrieved " + allUsers.size() + " users");
                }
            } catch (Exception e) {
                log.warn("JakartaDashboard: Error getting all users: " + e.getMessage(), e);
            }
            context.put("allUsers", allUsers);
            
            // Load all groups (same as Admin Dashboard)
            List<Group> allGroups = new ArrayList<>();
            try {
                GroupSet groups = security.getAllGroups();
                if (groups != null) {
                    for (Group group : groups) {
                        allGroups.add(group);
                    }
                    log.info("JakartaDashboard: Retrieved " + allGroups.size() + " groups");
                }
            } catch (Exception e) {
                log.warn("JakartaDashboard: Error getting all groups: " + e.getMessage(), e);
            }
            context.put("allGroups", allGroups);
            
            // Load all roles (same as Admin Dashboard)
            List<Role> allRoles = new ArrayList<>();
            try {
                RoleSet roles = security.getAllRoles();
                if (roles != null) {
                    for (Role role : roles) {
                        allRoles.add(role);
                    }
                    log.info("JakartaDashboard: Retrieved " + allRoles.size() + " roles");
                }
            } catch (Exception e) {
                log.warn("JakartaDashboard: Error getting all roles: " + e.getMessage(), e);
            }
            context.put("allRoles", allRoles);
            
            // Load all permissions (same as Admin Dashboard)
            List<Permission> allPermissions = new ArrayList<>();
            try {
                PermissionSet permissions = security.getAllPermissions();
                if (permissions != null) {
                    for (Permission permission : permissions) {
                        allPermissions.add(permission);
                    }
                    log.info("JakartaDashboard: Retrieved " + allPermissions.size() + " permissions");
                }
            } catch (Exception e) {
                log.warn("JakartaDashboard: Error getting all permissions: " + e.getMessage(), e);
            }
            context.put("allPermissions", allPermissions);
            
            log.info("JakartaDashboard: Dashboard loaded for user: " + user.getName() + 
                     " with " + allUsers.size() + " users, " + allGroups.size() + " groups, " +
                     allRoles.size() + " roles, " + allPermissions.size() + " permissions");
        }
        catch (Exception e)
        {
            log.error("Error loading Jakarta dashboard", e);
            runData.setMessage("Error loading Jakarta dashboard: " + e.getMessage());
        }
    }
}
