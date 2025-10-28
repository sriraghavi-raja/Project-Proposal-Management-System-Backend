package com.proposalmanagement.dto;

import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Represents different sets of fields that can be updated in a user's profile based on their role.
 * 
 * Access Levels:
 * 1. BASIC_INFO: Personal information that any user can update (firstName, lastName, email, phoneNumber)
 * 2. PROFESSIONAL_INFO: Professional details that some roles can update (organizationId, officeLocation, expertiseAreas)
 * 3. RESTRICTED_INFO: Information that only ADMIN and DEPARTMENT_HEAD can update (department)
 */
public class UpdateProfileRequest {
    
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Size(max = 15, message = "Phone number must be less than 15 characters")
    private String phoneNumber;
    
    @Size(max = 20, message = "Organization ID must be less than 20 characters")
    private String organizationId;
    
    @Size(max = 100, message = "Office location must be less than 100 characters")
    private String officeLocation;
    
    private String expertiseAreas;
    
    private Department department;
    
    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getOfficeLocation() {
        return officeLocation;
    }
    
    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }
    
    public String getExpertiseAreas() {
        return expertiseAreas;
    }
    
    public void setExpertiseAreas(String expertiseAreas) {
        this.expertiseAreas = expertiseAreas;
    }
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    /**
     * Validates if the current user role has permission to update the requested fields
     */
    public boolean validateUpdates(User.Role userRole) {
        // Check permissions based on role first
        switch (userRole) {
            case ADMIN:
            case DEPARTMENT_HEAD:
                // Admin and Department Head can update everything
                return true;
                
            case PROJECT_MANAGER:
            case PRINCIPAL_INVESTIGATOR:
            case COMMITTEE_CHAIR:
            case FINANCIAL_OFFICER:
                // Allow basic info and professional info, but not department
                if (department != null) {
                    return false;
                }
                return true;
                
            case REVIEWER:
            case STAKEHOLDER:
                // These roles can only update basic info
                if (organizationId != null || officeLocation != null || 
                    expertiseAreas != null || department != null) {
                    return false;
                }
                return true;
                
            default:
                return false;
        }
    }
}