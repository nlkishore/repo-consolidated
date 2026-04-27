package com.uob.service;

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
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.apache.turbine.om.security.User;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.security.SecurityService;
import org.springframework.stereotype.Service;

import com.uob.service.adapter.UserAdapter;

/**
 * Service layer that wraps Turbine Security Service
 * Provides business logic for security operations
 */
@Service
public class TurbineSecurityService {

    private static final Log log = LogFactory.getLog(TurbineSecurityService.class);

    /**
     * Get SecurityService instance
     */
    private SecurityService getSecurityService() {
        return (SecurityService) TurbineServices.getInstance().getService(SecurityService.SERVICE_NAME);
    }


    /**
     * Get all users
     */
    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            org.apache.torque.criteria.Criteria criteria = new org.apache.torque.criteria.Criteria();
            List<?> userList = securityService.getUserManager().retrieveList(criteria);
            // Convert to Turbine User list
            List<User> users = new ArrayList<>();
            for (Object obj : userList) {
                if (obj instanceof User) {
                    users.add((User) obj);
                }
            }
            return users;
        } catch (Exception e) {
            log.error("Error getting all users", e);
            throw new FulcrumSecurityException("Error retrieving users", e);
        }
    }

    /**
     * Get user by login name
     */
    public User getUserByLoginName(String loginName) throws UnknownEntityException {
        try {
            SecurityService securityService = getSecurityService();
            // getUser returns Turbine User (GtpUser) which implements both interfaces
            org.apache.fulcrum.security.entity.User fulcrumUser = securityService.getUser(loginName);
            // Cast to Turbine User - this is safe because GtpUser implements TurbineUser
            if (fulcrumUser instanceof User) {
                return (User) fulcrumUser;
            }
            // If not directly a Turbine User, it should still be compatible
            return (User) fulcrumUser;
        } catch (ClassCastException e) {
            log.error("User is not a Turbine User: " + loginName, e);
            throw new UnknownEntityException("User not found: " + loginName, e);
        } catch (UnknownEntityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting user: " + loginName, e);
            throw new UnknownEntityException("User not found: " + loginName, e);
        }
    }

    /**
     * Get all groups
     */
    public List<Group> getAllGroups() throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            org.apache.fulcrum.security.util.GroupSet groupSet = securityService.getAllGroups();
            return new ArrayList<>(groupSet.getSet());
        } catch (Exception e) {
            log.error("Error getting all groups", e);
            throw new FulcrumSecurityException("Error retrieving groups", e);
        }
    }

    /**
     * Get group by name
     */
    public Group getGroupByName(String groupName) throws UnknownEntityException {
        try {
            SecurityService securityService = getSecurityService();
            return securityService.getGroupByName(groupName);
        } catch (Exception e) {
            log.error("Error getting group: " + groupName, e);
            throw new UnknownEntityException("Group not found: " + groupName, e);
        }
    }

    /**
     * Get groups for a user
     */
    public List<Group> getUserGroups(User user) throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            // getACL() accepts Turbine User (GtpUser implements both interfaces)
            org.apache.fulcrum.security.model.turbine.TurbineAccessControlList acl = 
                (org.apache.fulcrum.security.model.turbine.TurbineAccessControlList) 
                securityService.getUserManager().getACL(user);
            // TurbineAccessControlList doesn't have getGroups() method
            // Instead, get all groups and check which ones the user belongs to
            org.apache.fulcrum.security.util.GroupSet allGroups = securityService.getAllGroups();
            List<Group> userGroups = new ArrayList<>();
            
            if (acl != null && allGroups != null) {
                for (Group group : allGroups) {
                    try {
                        // Check if user has any roles in this group
                        org.apache.fulcrum.security.util.RoleSet roles = acl.getRoles(group);
                        if (roles != null && !roles.isEmpty()) {
                            userGroups.add(group);
                        }
                    } catch (Exception ex) {
                        // Skip this group if error occurs
                        log.debug("Error checking roles for group: " + group.getName(), ex);
                    }
                }
            }
            
            return userGroups;
        } catch (Exception e) {
            log.error("Error getting groups for user: " + user.getName(), e);
            throw new FulcrumSecurityException("Error retrieving user groups", e);
        }
    }

    /**
     * Get all roles
     */
    public List<Role> getAllRoles() throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            org.apache.fulcrum.security.util.RoleSet roleSet = securityService.getAllRoles();
            return new ArrayList<>(roleSet.getSet());
        } catch (Exception e) {
            log.error("Error getting all roles", e);
            throw new FulcrumSecurityException("Error retrieving roles", e);
        }
    }

    /**
     * Get role by name
     */
    public Role getRoleByName(String roleName) throws UnknownEntityException {
        try {
            SecurityService securityService = getSecurityService();
            return securityService.getRoleByName(roleName);
        } catch (Exception e) {
            log.error("Error getting role: " + roleName, e);
            throw new UnknownEntityException("Role not found: " + roleName, e);
        }
    }

    /**
     * Get roles for a user (from all groups)
     */
    public List<Role> getUserRoles(User user) throws FulcrumSecurityException {
        Set<Role> allRoles = new HashSet<>();
        List<Group> groups = getUserGroups(user);
        SecurityService securityService = getSecurityService();
        
        try {
            // getACL() accepts Turbine User (GtpUser implements both interfaces)
            org.apache.fulcrum.security.model.turbine.TurbineAccessControlList acl = 
                (org.apache.fulcrum.security.model.turbine.TurbineAccessControlList) 
                securityService.getUserManager().getACL(user);
            
            if (acl != null) {
                for (Group group : groups) {
                    try {
                        org.apache.fulcrum.security.util.RoleSet roleSet = acl.getRoles(group);
                        if (roleSet != null) {
                            allRoles.addAll(roleSet.getSet());
                        }
                    } catch (Exception e) {
                        log.warn("Error getting roles for group: " + group.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting roles for user: " + user.getName(), e);
        }
        
        return new ArrayList<>(allRoles);
    }

    /**
     * Get all permissions
     */
    public List<Permission> getAllPermissions() throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            org.apache.fulcrum.security.util.PermissionSet permissionSet = securityService.getAllPermissions();
            return new ArrayList<>(permissionSet.getSet());
        } catch (Exception e) {
            log.error("Error getting all permissions", e);
            throw new FulcrumSecurityException("Error retrieving permissions", e);
        }
    }

    /**
     * Get permission by name
     */
    public Permission getPermissionByName(String permissionName) throws UnknownEntityException {
        try {
            SecurityService securityService = getSecurityService();
            return securityService.getPermissionByName(permissionName);
        } catch (Exception e) {
            log.error("Error getting permission: " + permissionName, e);
            throw new UnknownEntityException("Permission not found: " + permissionName, e);
        }
    }

    /**
     * Get permissions for a user (from all roles)
     */
    public List<Permission> getUserPermissions(User user) throws FulcrumSecurityException {
        Set<Permission> allPermissions = new HashSet<>();
        List<Role> roles = getUserRoles(user);
        SecurityService securityService = getSecurityService();
        
        for (Role role : roles) {
            try {
                org.apache.fulcrum.security.util.PermissionSet permissionSet = securityService.getPermissions(role);
                allPermissions.addAll(permissionSet.getSet());
            } catch (Exception e) {
                log.warn("Error getting permissions for role: " + role.getName(), e);
            }
        }
        
        return new ArrayList<>(allPermissions);
    }

    /**
     * Get permissions for a role
     */
    public List<Permission> getRolePermissions(Role role) throws FulcrumSecurityException {
        try {
            SecurityService securityService = getSecurityService();
            org.apache.fulcrum.security.util.PermissionSet permissionSet = securityService.getPermissions(role);
            return new ArrayList<>(permissionSet.getSet());
        } catch (Exception e) {
            log.error("Error getting permissions for role: " + role.getName(), e);
            throw new FulcrumSecurityException("Error retrieving role permissions", e);
        }
    }
}
