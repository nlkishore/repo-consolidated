package com.uob.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Permission;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uob.dto.PermissionDTO;
import com.uob.service.TurbineSecurityService;

/**
 * REST Controller for Permission operations
 * Exposes REST APIs to list and retrieve permissions
 */
@RestController
@RequestMapping("/permissions")
public class PermissionRestController {

    private static final Log log = LogFactory.getLog(PermissionRestController.class);

    @Autowired
    private TurbineSecurityService turbineSecurityService;

    /**
     * Get all permissions
     * GET /api/permissions
     */
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        try {
            List<Permission> permissions = turbineSecurityService.getAllPermissions();
            List<PermissionDTO> permissionDTOs = new ArrayList<>();
            
            for (Permission permission : permissions) {
                permissionDTOs.add(convertToDTO(permission));
            }
            
            return ResponseEntity.ok(permissionDTOs);
        } catch (FulcrumSecurityException e) {
            log.error("Error retrieving permissions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get permission by name
     * GET /api/permissions/{permissionName}
     */
    @GetMapping("/{permissionName}")
    public ResponseEntity<PermissionDTO> getPermissionByName(@PathVariable String permissionName) {
        try {
            Permission permission = turbineSecurityService.getPermissionByName(permissionName);
            return ResponseEntity.ok(convertToDTO(permission));
        } catch (UnknownEntityException e) {
            log.warn("Permission not found: " + permissionName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving permission: " + permissionName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Permission entity to DTO
     */
    private PermissionDTO convertToDTO(Permission permission) {
        PermissionDTO dto = new PermissionDTO();
        
        // Handle ID conversion (getId() returns Object)
        Object id = permission.getId();
        if (id instanceof Integer) {
            dto.setPermissionId((Integer) id);
        } else if (id != null) {
            dto.setPermissionId(Integer.valueOf(id.toString()));
        }
        
        dto.setPermissionName(permission.getName());
        return dto;
    }
}
