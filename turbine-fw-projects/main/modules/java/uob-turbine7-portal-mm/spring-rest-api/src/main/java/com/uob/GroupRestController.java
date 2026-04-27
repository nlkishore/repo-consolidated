package com.uob.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.util.FulcrumSecurityException;
import org.apache.fulcrum.security.util.UnknownEntityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uob.dto.GroupDTO;
import com.uob.service.TurbineSecurityService;

/**
 * REST Controller for Group operations
 * Exposes REST APIs to list and retrieve groups
 */
@RestController
@RequestMapping("/groups")
public class GroupRestController {

    private static final Log log = LogFactory.getLog(GroupRestController.class);

    @Autowired
    private TurbineSecurityService turbineSecurityService;

    /**
     * Get all groups
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        try {
            List<Group> groups = turbineSecurityService.getAllGroups();
            List<GroupDTO> groupDTOs = new ArrayList<>();
            
            for (Group group : groups) {
                groupDTOs.add(convertToDTO(group));
            }
            
            return ResponseEntity.ok(groupDTOs);
        } catch (FulcrumSecurityException e) {
            log.error("Error retrieving groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get group by name
     * GET /api/groups/{groupName}
     */
    @GetMapping("/{groupName}")
    public ResponseEntity<GroupDTO> getGroupByName(@PathVariable String groupName) {
        try {
            Group group = turbineSecurityService.getGroupByName(groupName);
            return ResponseEntity.ok(convertToDTO(group));
        } catch (UnknownEntityException e) {
            log.warn("Group not found: " + groupName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving group: " + groupName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Group entity to DTO
     */
    private GroupDTO convertToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        
        // Handle ID conversion (getId() returns Object)
        Object id = group.getId();
        if (id instanceof Integer) {
            dto.setGroupId((Integer) id);
        } else if (id != null) {
            dto.setGroupId(Integer.valueOf(id.toString()));
        }
        
        dto.setGroupName(group.getName());
        return dto;
    }
}
