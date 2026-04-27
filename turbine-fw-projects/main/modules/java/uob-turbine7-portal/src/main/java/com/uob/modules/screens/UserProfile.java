package com.uob.modules.screens;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

/**
 * User Profile Screen - Displays user's groups, roles, and permissions
 */
public class UserProfile extends com.uob.modules.screens.SecureScreen
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
        RunData runData = (RunData) data;
        try
        {
            SecurityService security = getSecurityService();
            User user = runData.getUser();
            
            if (user == null || user.getName().equals(security.getAnonymousUser().getName()))
            {
                runData.setMessage("Please login to view your profile");
                runData.setScreen("Login");
                return;
            }

            // Get user's groups using ACL
            List<Group> groups = new ArrayList<>();
            // getACL() accepts Turbine User (GtpUser implements both interfaces)
            org.apache.fulcrum.security.model.turbine.TurbineAccessControlList acl = 
                (org.apache.fulcrum.security.model.turbine.TurbineAccessControlList) 
                security.getUserManager().getACL(user);
            
            try
            {
                if (acl != null) {
                    // TurbineAccessControlList doesn't have getGroups() method
                    // Get all groups and check which ones the user belongs to
                    org.apache.fulcrum.security.util.GroupSet allGroups = security.getAllGroups();
                    if (allGroups != null) {
                        for (Group group : allGroups) {
                            try {
                                // Check if user has any roles in this group
                                org.apache.fulcrum.security.util.RoleSet roles = acl.getRoles(group);
                                if (roles != null && !roles.isEmpty()) {
                                    groups.add(group);
                                }
                            } catch (Exception ex) {
                                // Skip this group if error occurs
                                log.debug("Error checking roles for group: " + group.getName(), ex);
                            }
                        }
                    }
                }
            }
            catch (FulcrumSecurityException e)
            {
                log.warn("Error getting groups for user: " + user.getName(), e);
            }

            // Get user's roles (from all groups) using ACL
            Set<Role> allRoles = new HashSet<>();
            List<String> roleNames = new ArrayList<>();
            if (acl != null) {
                for (Group group : groups)
                {
                    try
                    {
                        org.apache.fulcrum.security.util.RoleSet roleSet = acl.getRoles(group);
                        if (roleSet != null) {
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
            Set<Permission> allPermissions = new HashSet<>();
            List<String> permissionNames = new ArrayList<>();
            for (Role role : allRoles)
            {
                try
                {
                    org.apache.fulcrum.security.util.PermissionSet permissionSet = security.getPermissions(role);
                    allPermissions.addAll(permissionSet.getSet());
                    for (Permission perm : permissionSet.getSet())
                    {
                        permissionNames.add(perm.getName());
                    }
                }
                catch (FulcrumSecurityException e)
                {
                    log.warn("Error getting permissions for role: " + role.getName(), e);
                }
            }

            // Put data into context for Velocity template
            context.put("user", user);
            context.put("groups", groups);
            context.put("roles", new ArrayList<>(allRoles));
            context.put("permissions", new ArrayList<>(allPermissions));
            context.put("roleNames", roleNames);
            context.put("permissionNames", permissionNames);
            
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
