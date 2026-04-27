package com.uob.modules.screens;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.model.turbine.TurbineAccessControlList;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.torque.criteria.Criteria;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;


/**
 * User Assignment Screen - Allows Managers to assign Groups, Roles, and Permissions to Users
 * 
 * This screen provides a comprehensive interface for managers to:
 * - Select a user
 * - Assign/remove groups
 * - Assign/remove roles within groups
 * - View permissions associated with roles
 */
public class UserAssignment extends SecureScreen
{
    /** Logging */
    private static Log log = LogFactory.getLog(UserAssignment.class);

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
        
        log.info("UserAssignment.isAuthorized() called for user: " + (user != null ? user.getName() : "null"));
        
        if (user == null || !user.hasLoggedIn()) {
            log.warn("UserAssignment: User is not logged in - redirecting to login");
            data.setMessage("Please login to access user assignment");
            data.setScreen("Login");
            return false;
        }
        
        // Check if user has admin or manager role
        TurbineAccessControlList acl = (TurbineAccessControlList) data.getACL();
        
        if (acl == null) {
            log.warn("UserAssignment: ACL is null - denying access");
            data.setMessage("You do not have permission to access user assignment");
            return false;
        }
        
        // Check role-based authorization (case-insensitive)
        boolean isAdmin = acl.hasRole("turbineadmin") || 
                         acl.hasRole("admin") || 
                         acl.hasRole("ADMIN") || 
                         acl.hasRole("Admin");
        boolean isManager = acl.hasRole("manager") || 
                           acl.hasRole("MANAGER") || 
                           acl.hasRole("Manager");
        
        // Username fallback (same as FluxScreen and FluxAction)
        boolean usernameHasAdmin = false;
        try {
            if (user != null && user.getName() != null) {
                String username = user.getName().toLowerCase();
                if (username.contains("admin") || username.contains("manager")) {
                    usernameHasAdmin = true;
                    log.info("UserAssignment: Username contains 'admin' or 'manager': " + user.getName());
                }
            }
        } catch (Exception e) {
            log.warn("UserAssignment: Error checking username: " + e.getMessage());
        }
        
        if (isAdmin || isManager || usernameHasAdmin) {
            log.info("UserAssignment: User " + user.getName() + " is authorized (admin: " + isAdmin + ", manager: " + isManager + ", username: " + usernameHasAdmin + ")");
            return true;
        } else {
            log.warn("UserAssignment: User " + user.getName() + " does not have admin or manager role - denying access");
            data.setMessage("You must be an Administrator or Manager to access user assignment");
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
        log.info("UserAssignment.doBuildTemplate() called");
        
        RunData runData = (RunData) data;
        try
        {
            SecurityService security = getSecurityService();
            
            // Get all users for the dropdown using SecurityService UserManager
            List<User> allUsers = new ArrayList<>();
            try {
                // Use SecurityService UserManager to retrieve users (same approach as FluxTool)
                Criteria criteria = new Criteria();
                @SuppressWarnings("unchecked")
                List<User> users = (List<User>) security.getUserManager().retrieveList(criteria);
                if (users != null) {
                    allUsers.addAll(users);
                    log.info("UserAssignment: Retrieved " + allUsers.size() + " users via getUserManager().retrieveList()");
                } else {
                    log.warn("UserAssignment: retrieveList() returned null");
                }
            } catch (Exception e) {
                log.warn("UserAssignment: Error getting all users: " + e.getMessage(), e);
            }
            
            // Get selected user from parameters
            String selectedUsername = runData.getParameters().getString("selectedUser");
            log.info("UserAssignment: selectedUsername parameter: " + (selectedUsername != null ? "'" + selectedUsername + "'" : "null"));
            
            User selectedUser = null;
            TurbineAccessControlList userACL = null;
            
            // Direct database query for user assignments (like UserProfile)
            Map<Integer, List<Integer>> userGroupRoleMap = new LinkedHashMap<>();
            List<Integer> assignedGroupIds = new ArrayList<>();
            List<Integer> assignedRoleIds = new ArrayList<>();
            
            if (selectedUsername != null && !selectedUsername.isEmpty()) {
                try {
                    log.info("UserAssignment: Attempting to get user: " + selectedUsername);
                    selectedUser = security.getUser(selectedUsername);
                    if (selectedUser != null) {
                        log.info("UserAssignment: User found: " + selectedUser.getName() + " (ID: " + selectedUser.getId() + ")");
                        
                        // Try ACL first (for compatibility)
                        try {
                            userACL = (TurbineAccessControlList) security.getUserManager().getACL(selectedUser);
                            if (userACL != null) {
                                org.apache.fulcrum.security.util.RoleSet roles = userACL.getRoles();
                                log.info("UserAssignment: User ACL contains " + (roles != null ? roles.size() : 0) + " role(s)");
                            }
                        } catch (Exception e) {
                            log.warn("UserAssignment: Error getting ACL: " + e.getMessage());
                        }
                        
                        // Direct database query (more reliable)
                        try {
                            Integer userId = Integer.valueOf(selectedUser.getId().toString());
                            Criteria criteria = new Criteria();
                            criteria.where(com.uob.om.GtpUserGroupRolePeer.USER_ID, userId);
                            
                            List<com.uob.om.GtpUserGroupRole> userGroupRoles = 
                                com.uob.om.GtpUserGroupRolePeer.doSelect(criteria);
                            log.info("UserAssignment: Direct DB query found " + userGroupRoles.size() + 
                                     " user-group-role records for user " + selectedUsername);
                            
                            for (com.uob.om.GtpUserGroupRole ugr : userGroupRoles) {
                                Integer groupId = ugr.getGroupId();
                                Integer roleId = ugr.getRoleId();
                                
                                if (!assignedGroupIds.contains(groupId)) {
                                    assignedGroupIds.add(groupId);
                                }
                                if (!assignedRoleIds.contains(roleId)) {
                                    assignedRoleIds.add(roleId);
                                }
                                
                                userGroupRoleMap.computeIfAbsent(groupId, k -> new ArrayList<>()).add(roleId);
                            }
                            
                            log.info("UserAssignment: User has " + assignedGroupIds.size() + " group(s) and " + 
                                     assignedRoleIds.size() + " role(s) assigned (from DB)");
                        } catch (Exception dbEx) {
                            log.warn("UserAssignment: Direct DB query failed: " + dbEx.getMessage(), dbEx);
                        }
                    } else {
                        log.warn("UserAssignment: User not found: " + selectedUsername);
                    }
                } catch (Exception e) {
                    log.error("UserAssignment: Error getting user " + selectedUsername + ": " + e.getMessage(), e);
                }
            } else {
                log.info("UserAssignment: No user selected yet");
            }
            
            // Get all groups and roles
            GroupSet allGroups = security.getAllGroups();
            RoleSet allRoles = security.getAllRoles();
            
            // Create data structures for the template
            List<Group> groups = new ArrayList<>();
            if (allGroups != null) {
                for (Group group : allGroups) {
                    groups.add(group);
                }
            }
            
            List<Role> roles = new ArrayList<>();
            if (allRoles != null) {
                for (Role role : allRoles) {
                    // Skip super_admin role
                    if (!"super_admin".equals(role.getName())) {
                        roles.add(role);
                    }
                }
            }
            
            // Put data into context
            context.put("allUsers", allUsers);
            context.put("selectedUser", selectedUser);
            context.put("selectedUsername", selectedUsername);
            context.put("userACL", userACL);
            context.put("groups", groups);
            context.put("roles", roles);
            
            // Direct DB query results for Current Assignments Summary
            context.put("userGroupRoleMap", userGroupRoleMap);
            context.put("assignedGroupIds", assignedGroupIds);
            context.put("assignedRoleIds", assignedRoleIds);
            
            log.info("UserAssignment: Loaded " + allUsers.size() + " users, " + 
                     groups.size() + " groups, " + roles.size() + " roles");
        }
        catch (Exception e)
        {
            log.error("Error loading user assignment data", e);
            runData.setMessage("Error loading user assignment: " + e.getMessage());
        }
    }
}
