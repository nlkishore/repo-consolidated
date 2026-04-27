package com.uob.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uob.dto.RoleDTO;
import com.uob.service.TurbineSecurityService;

/**
 * REST Controller for Role operations
 * Exposes REST APIs to list and retrieve roles
 */
@RestController
@RequestMapping("/roles")
public class RoleRestController {

    private static final Log log = LogFactory.getLog(RoleRestController.class);

    @Autowired
    private TurbineSecurityService turbineSecurityService;

    /**
     * Get all roles
     * GET /api/roles
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        try {
            List<Role> roles = turbineSecurityService.getAllRoles();
            List<RoleDTO> roleDTOs = new ArrayList<>();
            
            for (Role role : roles) {
                roleDTOs.add(convertToDTO(role));
            }
            
            return ResponseEntity.ok(roleDTOs);
        } catch (FulcrumSecurityException e) {
            log.error("Error retrieving roles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get role by name
     * GET /api/roles/{roleName}
     */
    @GetMapping("/{roleName}")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String roleName) {
        try {
            Role role = turbineSecurityService.getRoleByName(roleName);
            return ResponseEntity.ok(convertToDTO(role));
        } catch (UnknownEntityException e) {
            log.warn("Role not found: " + roleName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving role: " + roleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get permissions for a role
     * GET /api/roles/{roleName}/permissions
     */
    @GetMapping("/{roleName}/permissions")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable String roleName) {
        try {
            Role role = turbineSecurityService.getRoleByName(roleName);
            List<Permission> permissions = turbineSecurityService.getRolePermissions(role);
            List<String> permissionNames = new ArrayList<>();
            
            for (Permission permission : permissions) {
                permissionNames.add(permission.getName());
            }
            
            return ResponseEntity.ok(permissionNames);
        } catch (UnknownEntityException e) {
            log.warn("Role not found: " + roleName);
            return ResponseEntity.notFound().build();
        } catch (FulcrumSecurityException e) {
            log.error("Error retrieving permissions for role: " + roleName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Role entity to DTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        
        // Handle ID conversion (getId() returns Object)
        Object id = role.getId();
        if (id instanceof Integer) {
            dto.setRoleId((Integer) id);
        } else if (id != null) {
            dto.setRoleId(Integer.valueOf(id.toString()));
        }
        
        dto.setRoleName(role.getName());
        return dto;
    }
}
