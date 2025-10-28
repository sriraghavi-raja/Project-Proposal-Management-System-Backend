package com.proposalmanagement.service;

import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.Settings;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.repository.SettingsRepository;
import com.proposalmanagement.repository.UserRepository;
import com.proposalmanagement.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SettingsService {
    
    @Autowired
    private SettingsRepository settingsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    public Settings createSetting(Settings settings) {
        validateSettings(settings);
        settings.setIsActive(true);
        return settingsRepository.save(settings);
    }
    
    public Settings getSettingById(Long id) {
        return settingsRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));
    }
    
    public Optional<Settings> getSettingByKey(String settingKey) {
        return settingsRepository.findBySettingKeyAndIsActiveTrue(settingKey);
    }
    
    public String getSettingValue(String settingKey) {
        return getSettingByKey(settingKey)
            .map(Settings::getValueOrDefault)
            .orElse(null);
    }
    
    public String getSettingValue(String settingKey, String defaultValue) {
        return getSettingByKey(settingKey)
            .map(Settings::getValueOrDefault)
            .orElse(defaultValue);
    }
    
    public Optional<Settings> getUserSetting(String settingKey, Long userId) {
        return settingsRepository.findUserSetting(settingKey, userId);
    }
    
    public String getUserSettingValue(String settingKey, Long userId) {
        Optional<Settings> userSetting = getUserSetting(settingKey, userId);
        if (userSetting.isPresent()) {
            return userSetting.get().getValueOrDefault();
        }
        
        // Fall back to system setting if user setting doesn't exist
        return getSettingValue(settingKey);
    }
    
    public String getUserSettingValue(String settingKey, Long userId, String defaultValue) {
        String value = getUserSettingValue(settingKey, userId);
        return value != null ? value : defaultValue;
    }
    
    public Optional<Settings> getDepartmentSetting(String settingKey, Long departmentId) {
        return settingsRepository.findDepartmentSetting(settingKey, departmentId);
    }
    
    public String getDepartmentSettingValue(String settingKey, Long departmentId) {
        Optional<Settings> deptSetting = getDepartmentSetting(settingKey, departmentId);
        if (deptSetting.isPresent()) {
            return deptSetting.get().getValueOrDefault();
        }
        
        // Fall back to system setting if department setting doesn't exist
        return getSettingValue(settingKey);
    }
    
    public Optional<Settings> getSystemSetting(String settingKey) {
        return settingsRepository.findSystemSetting(settingKey);
    }
    
    public List<Settings> getSettingsByType(Settings.SettingType settingType) {
        return settingsRepository.findBySettingTypeAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(settingType);
    }
    
    public List<Settings> getSettingsByScope(Settings.Scope scope) {
        return settingsRepository.findByScopeAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(scope);
    }
    
    public List<Settings> getUserSettings(Long userId) {
        return settingsRepository.findUserSettings(userId);
    }
    
    public List<Settings> getDepartmentSettings(Long departmentId) {
        return settingsRepository.findDepartmentSettings(departmentId);
    }
    
    public List<Settings> getSystemSettings() {
        return settingsRepository.findSystemSettings();
    }
    
    public List<Settings> getSettingsByCategory(String category) {
        return settingsRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(category);
    }
    
    public List<Settings> getUserSettingsByCategory(String category, Long userId) {
        return settingsRepository.findUserSettingsByCategory(category, userId);
    }
    
    public List<Settings> getDepartmentSettingsByCategory(String category, Long departmentId) {
        return settingsRepository.findDepartmentSettingsByCategory(category, departmentId);
    }
    
    public List<String> getDistinctCategories() {
        return settingsRepository.findDistinctCategories();
    }
    
    public List<Settings> getEditableSettings() {
        return settingsRepository.findEditableSettings();
    }
    
    public List<Settings> getSettingsByDataType(Settings.DataType dataType) {
        return settingsRepository.findByDataType(dataType);
    }
    
    public List<Settings> searchSettings(String keyword) {
        return settingsRepository.searchByKeyword(keyword);
    }
    
    public Settings updateSetting(Long id, Settings settings) {
        Settings existingSetting = getSettingById(id);
        
        if (existingSetting.getIsReadonly()) {
            throw new InvalidOperationException("Cannot update read-only setting");
        }
        
        existingSetting.setSettingValue(settings.getSettingValue());
        existingSetting.setDescription(settings.getDescription());
        existingSetting.setDisplayOrder(settings.getDisplayOrder());
        existingSetting.setCategory(settings.getCategory());
        
        // Update user/department associations if provided
        if (settings.getUser() != null && settings.getUser().getId() != null) {
            User user = userRepository.findById(settings.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + settings.getUser().getId()));
            existingSetting.setUser(user);
        }
        
        if (settings.getDepartment() != null && settings.getDepartment().getId() != null) {
            Department department = departmentRepository.findById(settings.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + settings.getDepartment().getId()));
            existingSetting.setDepartment(department);
        }
        
        validateSettings(existingSetting);
        return settingsRepository.save(existingSetting);
    }
    
    public Settings updateSettingValue(String settingKey, String value) {
        Settings setting = getSettingByKey(settingKey)
            .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + settingKey));
        
        if (setting.getIsReadonly()) {
            throw new InvalidOperationException("Cannot update read-only setting: " + settingKey);
        }
        
        setting.setSettingValue(value);
        return settingsRepository.save(setting);
    }
    
    public Settings updateUserSettingValue(String settingKey, Long userId, String value) {
        Optional<Settings> userSettingOpt = getUserSetting(settingKey, userId);
        
        if (userSettingOpt.isPresent()) {
            Settings userSetting = userSettingOpt.get();
            userSetting.setSettingValue(value);
            return settingsRepository.save(userSetting);
        } else {
            // Create new user setting
            Optional<Settings> systemSettingOpt = getSystemSetting(settingKey);
            if (systemSettingOpt.isEmpty()) {
                throw new ResourceNotFoundException("System setting not found with key: " + settingKey);
            }
            
            Settings systemSetting = systemSettingOpt.get();
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
            Settings userSetting = new Settings();
            userSetting.setSettingKey(settingKey);
            userSetting.setSettingValue(value);
            userSetting.setSettingType(systemSetting.getSettingType());
            userSetting.setDataType(systemSetting.getDataType());
            userSetting.setScope(Settings.Scope.USER);
            userSetting.setUser(user);
            userSetting.setDescription(systemSetting.getDescription());
            userSetting.setCategory(systemSetting.getCategory());
            
            return createSetting(userSetting);
        }
    }
    
    public Settings createUserSetting(String settingKey, String value, Long userId, Settings.SettingType settingType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Settings userSetting = new Settings();
        userSetting.setSettingKey(settingKey);
        userSetting.setSettingValue(value);
        userSetting.setSettingType(settingType);
        userSetting.setScope(Settings.Scope.USER);
        userSetting.setUser(user);
        
        return createSetting(userSetting);
    }
    
    public Settings createSystemSetting(String settingKey, String value, Settings.SettingType settingType, Settings.DataType dataType) {
        Settings systemSetting = new Settings();
        systemSetting.setSettingKey(settingKey);
        systemSetting.setSettingValue(value);
        systemSetting.setSettingType(settingType);
        systemSetting.setDataType(dataType);
        systemSetting.setScope(Settings.Scope.SYSTEM);
        
        return createSetting(systemSetting);
    }
    
    public void deleteSetting(Long id) {
        Settings setting = getSettingById(id);
        setting.setIsActive(false);
        settingsRepository.save(setting);
    }
    
    public Long getUserSettingsCount(Long userId) {
        return settingsRepository.countUserSettings(userId);
    }
    
    public Long getDepartmentSettingsCount(Long departmentId) {
        return settingsRepository.countDepartmentSettings(departmentId);
    }
    
    // Utility methods for different data types
    public boolean getBooleanSetting(String settingKey, boolean defaultValue) {
        String value = getSettingValue(settingKey);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    public int getIntegerSetting(String settingKey, int defaultValue) {
        String value = getSettingValue(settingKey);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public double getDoubleSetting(String settingKey, double defaultValue) {
        String value = getSettingValue(settingKey);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private void validateSettings(Settings settings) {
        if (settings.getSettingKey() == null || settings.getSettingKey().trim().isEmpty()) {
            throw new InvalidOperationException("Setting key is required");
        }
        
        if (settings.getSettingType() == null) {
            throw new InvalidOperationException("Setting type is required");
        }
        
        if (settings.getScope() == null) {
            throw new InvalidOperationException("Setting scope is required");
        }
        
        // Validate scope constraints
        if (settings.getScope() == Settings.Scope.USER && settings.getUser() == null) {
            throw new InvalidOperationException("User is required for user-scoped settings");
        }
        
        if (settings.getScope() == Settings.Scope.DEPARTMENT && settings.getDepartment() == null) {
            throw new InvalidOperationException("Department is required for department-scoped settings");
        }
        
        if (settings.getScope() == Settings.Scope.SYSTEM && (settings.getUser() != null || settings.getDepartment() != null)) {
            throw new InvalidOperationException("System-scoped settings cannot be associated with user or department");
        }
    }
}