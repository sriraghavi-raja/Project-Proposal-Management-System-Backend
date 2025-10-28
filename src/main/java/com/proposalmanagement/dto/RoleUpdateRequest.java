package com.proposalmanagement.dto;

import com.proposalmanagement.entity.User;
import jakarta.validation.constraints.NotNull;

public class RoleUpdateRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "New role is required")
    private User.Role newRole;
    
    private String reason; // Optional reason for role change
    
    // Constructors
    public RoleUpdateRequest() {}
    
    public RoleUpdateRequest(Long userId, User.Role newRole, String reason) {
        this.userId = userId;
        this.newRole = newRole;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public User.Role getNewRole() {
        return newRole;
    }
    
    public void setNewRole(User.Role newRole) {
        this.newRole = newRole;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}