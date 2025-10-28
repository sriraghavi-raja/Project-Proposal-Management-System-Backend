package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    
    Optional<Settings> findBySettingKeyAndIsActiveTrue(String settingKey);
    
    @Query("SELECT s FROM Settings s WHERE s.settingKey = :settingKey AND s.user.id = :userId AND s.isActive = true")
    Optional<Settings> findUserSetting(@Param("settingKey") String settingKey, @Param("userId") Long userId);
    
    @Query("SELECT s FROM Settings s WHERE s.settingKey = :settingKey AND s.department.id = :departmentId AND s.isActive = true")
    Optional<Settings> findDepartmentSetting(@Param("settingKey") String settingKey, @Param("departmentId") Long departmentId);
    
    @Query("SELECT s FROM Settings s WHERE s.settingKey = :settingKey AND s.user IS NULL AND s.department IS NULL AND s.isActive = true")
    Optional<Settings> findSystemSetting(@Param("settingKey") String settingKey);
    
    List<Settings> findBySettingTypeAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(Settings.SettingType settingType);
    
    List<Settings> findByScopeAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(Settings.Scope scope);
    
    @Query("SELECT s FROM Settings s WHERE s.user.id = :userId AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findUserSettings(@Param("userId") Long userId);
    
    @Query("SELECT s FROM Settings s WHERE s.department.id = :departmentId AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findDepartmentSettings(@Param("departmentId") Long departmentId);
    
    @Query("SELECT s FROM Settings s WHERE s.user IS NULL AND s.department IS NULL AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findSystemSettings();
    
    List<Settings> findByCategoryAndIsActiveTrueOrderByDisplayOrderAscSettingKeyAsc(String category);
    
    @Query("SELECT s FROM Settings s WHERE s.category = :category AND s.user.id = :userId AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findUserSettingsByCategory(@Param("category") String category, @Param("userId") Long userId);
    
    @Query("SELECT s FROM Settings s WHERE s.category = :category AND s.department.id = :departmentId AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findDepartmentSettingsByCategory(@Param("category") String category, @Param("departmentId") Long departmentId);
    
    @Query("SELECT DISTINCT s.category FROM Settings s WHERE s.isActive = true AND s.category IS NOT NULL ORDER BY s.category")
    List<String> findDistinctCategories();
    
    @Query("SELECT s FROM Settings s WHERE s.isReadonly = false AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findEditableSettings();
    
    @Query("SELECT s FROM Settings s WHERE s.dataType = :dataType AND s.isActive = true ORDER BY s.displayOrder ASC, s.settingKey ASC")
    List<Settings> findByDataType(@Param("dataType") Settings.DataType dataType);
    
    Optional<Settings> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT s FROM Settings s WHERE s.settingKey LIKE %:keyword% OR s.description LIKE %:keyword% AND s.isActive = true ORDER BY s.settingKey ASC")
    List<Settings> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(s) FROM Settings s WHERE s.user.id = :userId AND s.isActive = true")
    Long countUserSettings(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(s) FROM Settings s WHERE s.department.id = :departmentId AND s.isActive = true")  
    Long countDepartmentSettings(@Param("departmentId") Long departmentId);
}