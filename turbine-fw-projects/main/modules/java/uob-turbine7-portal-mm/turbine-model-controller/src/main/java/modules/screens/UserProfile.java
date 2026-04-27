package com.uob.modules.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.turbine.annotation.TurbineService;
import org.apache.turbine.om.security.User;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.services.security.SecurityService;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;

import com.uob.om.GtpUserGroupRole;
import com.uob.om.GtpUserGroupRolePeer;
import com.uob.om.GtpGroupPeer;
import com.uob.om.GtpRolePeer;
import org.apache.torque.criteria.Criteria;

/**
 * User Profile Screen - Displays user's groups, roles, and permissions
 * 
 * Enhanced to show hierarchical relationships:
 * - Groups with their associated Roles
 * - Roles with their associated Permissions
 * - Summary views for quick reference
 */
public class UserProfile extends SecureScreen
{
    /** Logging */
    private static Log log = LogFactory.getLog(UserProfile.class);

    /**
     * Get SecurityService instance
     */
    private SecurityService getSecurityService() {
        return (SecurityService) org.apache.turbine.services.TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);
    }

    /**
     * Override isAuthorized to allow all logged-in users to view their profile
     * and add logging to verify screen class is being invoked
     */
    @Override
    protected boolean isAuthorized(PipelineData pipelineData) throws Exception {
        log.info("========================================");
        log.info("UserProfile.isAuthorized() CALLED!");
        log.info("========================================");
        
        RunData data = (RunData) pipelineData;
        User user = data.getUser();
        
        if (user != null && user.hasLoggedIn()) {
            log.info("UserProfile: User " + user.getName() + " is authorized");
            return true;
        }
        
        log.warn("UserProfile: User not logged in or null");
        return super.isAuthorized(pipelineData);
    }

    /**
     * This method is called by the Turbine framework when the
     * screen is accessed.
     *
     * @param data the RunData object
     * @param context the Velocity context
     * @exception Exception a generic exception
     */
    @Override
    protected void doBuildTemplate(PipelineData data, Context context)
            throws Exception
    {
        log.info("========================================");
        log.info("UserProfile.doBuildTemplate() CALLED!");
        log.info("========================================");
        
        RunData runData = (RunData) data;
        try
        {
            SecurityService security = getSecurityService();
            User user = runData.getUser();
            
            log.info("UserProfile: User object: " + (user != null ? user.getName() : "null"));
            
            if (user == null || user.getName().equals(security.getAnonymousUser().getName()))
            {
                log.warn("UserProfile: User is null or anonymous - redirecting to login");
                runData.setMessage("Please login to view your profile");
                runData.setScreen("Login");
                return;
            }

            // Get user's groups using ACL and direct database query
            List<Group> groups = new ArrayList<>();
            // getACL() accepts Turbine User (GtpUser implements both interfaces)
            @SuppressWarnings("unchecked")
            org.apache.fulcrum.security.model.turbine.TurbineAccessControlList acl = 
                (org.apache.fulcrum.security.model.turbine.TurbineAccessControlList) 
                security.getUserManager().getACL(user);
            
            log.info("UserProfile: Loading profile for user: " + user.getName() + " (ID: " + user.getId() + ")");
            log.info("UserProfile: ACL retrieved: " + (acl != null ? "Yes" : "No"));
            
            try
            {
                // Try direct database query first to get user's groups
                try {
                    Integer userId = Integer.valueOf(user.getId().toString());
                    Criteria criteria = new Criteria();
                    criteria.where(GtpUserGroupRolePeer.USER_ID, userId);
                    // Don't use GROUP BY - just select all records and process in Java
                    
                    List<GtpUserGroupRole> userGroupRoles = GtpUserGroupRolePeer.doSelect(criteria);
                    log.info("UserProfile: Direct DB query found " + userGroupRoles.size() + " group-role assignments for user " + user.getName());
                    
                    Set<Integer> groupIds = new HashSet<>();
                    for (GtpUserGroupRole ugr : userGroupRoles) {
                        groupIds.add(ugr.getGroupId());
                    }
                    
                    log.info("UserProfile: Found " + groupIds.size() + " unique groups for user " + user.getName());
                    
                    // Get Group objects for these group IDs
                    for (Integer groupId : groupIds) {
                        try {
                            Group group = security.getGroupById(groupId);
                            if (group != null && !groups.contains(group)) {
                                groups.add(group);
                                log.info("UserProfile: ✓ Found group via DB query: " + group.getName() + " (ID: " + groupId + ")");
                            }
                        } catch (Exception e) {
                            log.warn("UserProfile: Error getting group by ID " + groupId + ": " + e.getMessage(), e);
                        }
                    }
                } catch (Exception dbEx) {
                    log.warn("UserProfile: Direct DB query failed, falling back to ACL method: " + dbEx.getMessage(), dbEx);
                }
                
                // If no groups found via DB query, try ACL method
                if (groups.isEmpty() && acl != null) {
                    // TurbineAccessControlList doesn't have getGroups() method
                    // Get all groups and check which ones the user belongs to
                    org.apache.fulcrum.security.util.GroupSet allGroups = security.getAllGroups();
                    log.info("UserProfile: Total groups in system: " + (allGroups != null ? allGroups.size() : 0));
                    
                    if (allGroups != null) {
                        log.info("UserProfile: Checking " + allGroups.size() + " groups for user " + user.getName());
                        for (Group group : allGroups) {
                            try {
                                log.info("UserProfile: Checking group: " + group.getName() + " (ID: " + group.getId() + ", Type: " + group.getClass().getName() + ")");
                                // Check if user has any roles in this group
                                org.apache.fulcrum.security.util.RoleSet roles = acl.getRoles(group);
                                log.info("UserProfile: acl.getRoles(" + group.getName() + ") returned: " + 
                                    (roles == null ? "null" : (roles.isEmpty() ? "empty" : roles.size() + " roles")));
                                if (roles != null && !roles.isEmpty()) {
                                    if (!groups.contains(group)) {
                                        groups.add(group);
                                    }
                                    log.info("UserProfile: ✓ User " + user.getName() + " has roles in group: " + group.getName() + " (roles: " + roles.size() + ")");
                                } else {
                                    log.info("UserProfile: User " + user.getName() + " has no roles in group: " + group.getName());
                                }
                            } catch (Exception ex) {
                                // Skip this group if error occurs
                                log.warn("UserProfile: Error checking roles for group: " + group.getName() + " - " + ex.getMessage(), ex);
                                log.warn("UserProfile: Exception details: " + ex.getClass().getName(), ex);
                            }
                        }
                    }
                }
                
                log.info("UserProfile: Found " + groups.size() + " groups for user " + user.getName());
                
                if (groups.isEmpty()) {
                    log.warn("UserProfile: ⚠ No groups found for user " + user.getName() + 
                        " - User may not have group/role assignments in database. " +
                        "Please run assign-roles-to-all-users.sql script.");
                }
            }
            catch (FulcrumSecurityException e)
            {
                log.error("UserProfile: Error getting groups for user: " + user.getName(), e);
            }
            catch (Exception e)
            {
                log.error("UserProfile: Unexpected error getting groups for user: " + user.getName(), e);
            }

            // Get user's roles (from all groups) using ACL and database query
            // Also create group-to-roles mapping for display
            Set<Role> allRoles = new HashSet<>();
            List<String> roleNames = new ArrayList<>();
            Map<Group, List<Role>> groupRolesMap = new LinkedHashMap<>(); // Preserve order
            
            // First, try to get roles from database query
            try {
                Integer userId = Integer.valueOf(user.getId().toString());
                Criteria criteria = new Criteria();
                criteria.where(GtpUserGroupRolePeer.USER_ID, userId);
                
                List<GtpUserGroupRole> userGroupRoles = GtpUserGroupRolePeer.doSelect(criteria);
                log.info("UserProfile: Found " + userGroupRoles.size() + " user-group-role records in database");
                
                // Group by GROUP_ID and ROLE_ID
                Map<Integer, List<Integer>> groupRoleMap = new LinkedHashMap<>();
                for (GtpUserGroupRole ugr : userGroupRoles) {
                    Integer groupId = ugr.getGroupId();
                    Integer roleId = ugr.getRoleId();
                    groupRoleMap.computeIfAbsent(groupId, k -> new ArrayList<>()).add(roleId);
                }
                
                // Get Group and Role objects
                for (Map.Entry<Integer, List<Integer>> entry : groupRoleMap.entrySet()) {
                    Integer groupId = entry.getKey();
                    List<Integer> roleIds = entry.getValue();
                    
                    try {
                        Group group = security.getGroupById(groupId);
                        if (group != null) {
                            // Add group to groups list if not already there
                            if (!groups.contains(group)) {
                                groups.add(group);
                                log.info("UserProfile: ✓ Added group via roles query: " + group.getName() + " (ID: " + groupId + ")");
                            }
                            
                            List<Role> groupRoles = new ArrayList<>();
                            for (Integer roleId : roleIds) {
                                try {
                                    Role role = security.getRoleById(roleId);
                                    if (role != null) {
                                        groupRoles.add(role);
                                        allRoles.add(role);
                                        roleNames.add(role.getName());
                                        log.info("UserProfile: ✓ Found role via DB: " + role.getName() + " (ID: " + roleId + ") in group: " + group.getName());
                                    }
                                } catch (Exception e) {
                                    log.warn("UserProfile: Error getting role by ID " + roleId + ": " + e.getMessage(), e);
                                }
                            }
                            if (!groupRoles.isEmpty()) {
                                groupRolesMap.put(group, groupRoles);
                                log.info("UserProfile: ✓ Mapped " + groupRoles.size() + " role(s) to group: " + group.getName());
                            }
                        } else {
                            log.warn("UserProfile: Group with ID " + groupId + " not found");
                        }
                    } catch (Exception e) {
                        log.warn("UserProfile: Error getting group by ID " + groupId + ": " + e.getMessage(), e);
                    }
                }
            } catch (Exception dbEx) {
                log.warn("UserProfile: Database query for roles failed, falling back to ACL: " + dbEx.getMessage());
            }
            
            // Fallback to ACL method if database query didn't work
            if (groupRolesMap.isEmpty() && acl != null) {
                for (Group group : groups)
                {
                    try
                    {
                        org.apache.fulcrum.security.util.RoleSet roleSet = acl.getRoles(group);
                        if (roleSet != null && !roleSet.isEmpty()) {
                            List<Role> groupRoles = new ArrayList<>(roleSet.getSet());
                            groupRolesMap.put(group, groupRoles);
                            allRoles.addAll(roleSet.getSet());
                            for (Role role : roleSet.getSet())
                            {
                                roleNames.add(role.getName());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.warn("Error getting roles for group: " + group.getName(), e);
                    }
                }
            }

            // Get user's permissions (from all roles)
            // Also create role-to-permissions mapping for display
            Set<Permission> allPermissions = new HashSet<>();
            List<String> permissionNames = new ArrayList<>();
            Map<Role, List<Permission>> rolePermissionsMap = new LinkedHashMap<>(); // Preserve order
            
            for (Role role : allRoles)
            {
                try
                {
                    org.apache.fulcrum.security.util.PermissionSet permissionSet = security.getPermissions(role);
                    if (permissionSet != null && !permissionSet.isEmpty()) {
                        List<Permission> rolePerms = new ArrayList<>(permissionSet.getSet());
                        rolePermissionsMap.put(role, rolePerms);
                        allPermissions.addAll(permissionSet.getSet());
                        for (Permission perm : permissionSet.getSet())
                        {
                            permissionNames.add(perm.getName());
                        }
                    }
                }
                catch (FulcrumSecurityException e)
                {
                    log.warn("Error getting permissions for role: " + role.getName(), e);
                }
            }

            // Create hierarchical data structure for better display
            // Group -> Roles -> Permissions
            List<Map<String, Object>> groupDetails = new ArrayList<>();
            for (Map.Entry<Group, List<Role>> entry : groupRolesMap.entrySet()) {
                Group group = entry.getKey();
                List<Role> groupRoles = entry.getValue();
                
                List<Map<String, Object>> roleDetails = new ArrayList<>();
                for (Role role : groupRoles) {
                    List<Permission> rolePerms = rolePermissionsMap.get(role);
                    if (rolePerms == null) {
                        rolePerms = new ArrayList<>();
                    }
                    
                    Map<String, Object> roleInfo = new HashMap<>();
                    roleInfo.put("role", role);
                    roleInfo.put("permissions", rolePerms);
                    roleInfo.put("permissionCount", rolePerms.size());
                    roleDetails.add(roleInfo);
                }
                
                Map<String, Object> groupInfo = new HashMap<>();
                groupInfo.put("group", group);
                groupInfo.put("roles", roleDetails);
                groupInfo.put("roleCount", groupRoles.size());
                groupDetails.add(groupInfo);
            }

            // Put data into context for Velocity template
            context.put("user", user);
            context.put("groups", groups);
            context.put("roles", new ArrayList<>(allRoles));
            context.put("permissions", new ArrayList<>(allPermissions));
            context.put("roleNames", roleNames);
            context.put("permissionNames", permissionNames);
            
            // Enhanced data structures for hierarchical display
            context.put("groupRolesMap", groupRolesMap);
            context.put("rolePermissionsMap", rolePermissionsMap);
            context.put("groupDetails", groupDetails);
            
            log.info("User profile loaded for: " + user.getName() + 
                     " - Groups: " + groups.size() + 
                     ", Roles: " + allRoles.size() + 
                     ", Permissions: " + allPermissions.size());
        }
        catch (Exception e)
        {
            log.error("Error loading user profile", e);
            runData.setMessage("Error loading profile: " + e.getMessage());
        }
    }
}
