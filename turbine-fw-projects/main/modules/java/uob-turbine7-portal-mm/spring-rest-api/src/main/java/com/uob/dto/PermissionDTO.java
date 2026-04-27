package com.uob.dto;

/**
 * Data Transfer Object for Permission
 */
public class PermissionDTO {
    private Integer permissionId;
    private String permissionName;

    // Getters and Setters
    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}
