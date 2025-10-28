package com.proposalmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"setting_key", "user_id", "department_id"}))
public class Settings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;
    
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
    
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false)
    private SettingType settingType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType = DataType.STRING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private Scope scope = Scope.SYSTEM;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "validation_rule", length = 500)
    private String validationRule; // Regex or validation expression
    
    @Column(name = "possible_values", columnDefinition = "TEXT")
    private String possibleValues; // JSON array for dropdown options
    
    // For user-specific settings
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // For department-specific settings
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted = false;
    
    @Column(name = "is_readonly", nullable = false)
    private Boolean isReadonly = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    public enum SettingType {
        SYSTEM_CONFIG, USER_PREFERENCE, DEPARTMENT_CONFIG, SECURITY_SETTING,
        NOTIFICATION_SETTING, EMAIL_SETTING, WORKFLOW_SETTING, INTEGRATION_SETTING,
        PERFORMANCE_SETTING, ANALYTICS_SETTING, BACKUP_SETTING, APPEARANCE_SETTING
    }
    
    public enum DataType {
        STRING, INTEGER, DECIMAL, BOOLEAN, DATE, TIME, DATETIME, 
        JSON, EMAIL, URL, PASSWORD, COLOR, FILE_PATH
    }
    
    public enum Scope {
        SYSTEM, USER, DEPARTMENT, ROLE
    }
    
    // Constructors
    public Settings() {}
    
    public Settings(String settingKey, String settingValue, SettingType settingType) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
    }
    
    public Settings(String settingKey, String settingValue, SettingType settingType, Scope scope) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
        this.scope = scope;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSettingKey() {
        return settingKey;
    }
    
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }
    
    public String getSettingValue() {
        return settingValue;
    }
    
    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public SettingType getSettingType() {
        return settingType;
    }
    
    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }
    
    public DataType getDataType() {
        return dataType;
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    
    public Scope getScope() {
        return scope;
    }
    
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getValidationRule() {
        return validationRule;
    }
    
    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }
    
    public String getPossibleValues() {
        return possibleValues;
    }
    
    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Department getDepartment() {
        return department;
    }
    
    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public Boolean getIsEncrypted() {
        return isEncrypted;
    }
    
    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }
    
    public Boolean getIsReadonly() {
        return isReadonly;
    }
    
    public void setIsReadonly(Boolean isReadonly) {
        this.isReadonly = isReadonly;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public User getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    // Helper methods
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(settingValue);
    }
    
    public int getIntegerValue() {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public double getDoubleValue() {
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    public String getValueOrDefault() {
        return settingValue != null ? settingValue : defaultValue;
    }
}