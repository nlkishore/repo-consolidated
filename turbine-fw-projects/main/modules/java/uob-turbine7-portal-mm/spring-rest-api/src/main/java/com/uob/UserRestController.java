package com.uob.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.model.turbine.entity.TurbineUser;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uob.dto.UserDTO;
import com.uob.service.TurbineSecurityService;

/**
 * REST Controller for User operations
 * Exposes REST APIs to list and retrieve users
 */
@RestController
@RequestMapping("/users")
public class UserRestController {

    private static final Log log = LogFactory.getLog(UserRestController.class);

    @Autowired
    private TurbineSecurityService turbineSecurityService;

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<org.apache.turbine.om.security.User> users = turbineSecurityService.getAllUsers();
            List<UserDTO> userDTOs = new ArrayList<>();
            
            for (org.apache.turbine.om.security.User user : users) {
                userDTOs.add(convertToDTO(user));
            }
            
            return ResponseEntity.ok(userDTOs);
        } catch (FulcrumSecurityException e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by login name
     * GET /api/users/{loginName}
     */
    @GetMapping("/{loginName}")
    public ResponseEntity<UserDTO> getUserByLoginName(@PathVariable String loginName) {
        try {
            org.apache.turbine.om.security.User user = turbineSecurityService.getUserByLoginName(loginName);
            return ResponseEntity.ok(convertToDTO(user));
        } catch (UnknownEntityException e) {
            log.warn("User not found: " + loginName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving user: " + loginName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert User entity to DTO
     */
    private UserDTO convertToDTO(org.apache.turbine.om.security.User user) {
        UserDTO dto = new UserDTO();
        
        // Handle ID conversion (getId() returns Object)
        Object id = user.getId();
        if (id instanceof Integer) {
            dto.setUserId((Integer) id);
        } else if (id != null) {
            dto.setUserId(Integer.valueOf(id.toString()));
        }
        
        dto.setLoginName(user.getName());
        
        // Cast to TurbineUser (GtpUser) to access additional properties
        if (user instanceof com.uob.om.GtpUser) {
            com.uob.om.GtpUser gtpUser = (com.uob.om.GtpUser) user;
            dto.setFirstName(gtpUser.getFirstName());
            dto.setLastName(gtpUser.getLastName());
            dto.setEmail(gtpUser.getEmail());
            
            // getConfirmed() returns String, convert to Boolean
            String confirmed = gtpUser.getConfirmed();
            dto.setConfirmed("Y".equalsIgnoreCase(confirmed) || "true".equalsIgnoreCase(confirmed));
            
            dto.setLastLogin(gtpUser.getLastLogin());
            dto.setCreated(gtpUser.getCreateDate());
            dto.setModified(gtpUser.getModifiedDate());
        }
        
        return dto;
    }
}
