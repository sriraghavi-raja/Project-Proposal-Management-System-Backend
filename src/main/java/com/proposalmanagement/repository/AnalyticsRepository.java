package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    
    List<Analytics> findByMetricTypeAndIsActiveTrueOrderByRecordedDateDesc(Analytics.MetricType metricType);
    
    List<Analytics> findByMetricNameAndIsActiveTrueOrderByRecordedDateDesc(String metricName);
    
    @Query("SELECT a FROM Analytics a WHERE a.entityType = :entityType AND a.entityId = :entityId AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM Analytics a WHERE a.departmentId = :departmentId AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT a FROM Analytics a WHERE a.userId = :userId AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Analytics a WHERE a.recordedDate BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Analytics a WHERE a.metricType = :metricType AND a.recordedDate BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByMetricTypeAndDateRange(@Param("metricType") Analytics.MetricType metricType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Analytics a WHERE a.periodStart >= :periodStart AND a.periodEnd <= :periodEnd AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByPeriodRange(@Param("periodStart") LocalDateTime periodStart, @Param("periodEnd") LocalDateTime periodEnd);
    
    @Query("SELECT DISTINCT a.metricName FROM Analytics a WHERE a.metricType = :metricType AND a.isActive = true ORDER BY a.metricName")
    List<String> findDistinctMetricNamesByType(@Param("metricType") Analytics.MetricType metricType);
    
    @Query("SELECT DISTINCT a.entityType FROM Analytics a WHERE a.isActive = true ORDER BY a.entityType")
    List<String> findDistinctEntityTypes();
    
    @Query("SELECT a FROM Analytics a WHERE a.metricName = :metricName AND a.entityType = :entityType AND a.entityId = :entityId AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByMetricNameAndEntity(@Param("metricName") String metricName, @Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    Optional<Analytics> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT AVG(a.metricValue) FROM Analytics a WHERE a.metricName = :metricName AND a.recordedDate BETWEEN :startDate AND :endDate AND a.metricValue IS NOT NULL AND a.isActive = true")
    Double getAverageMetricValue(@Param("metricName") String metricName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(a.countValue) FROM Analytics a WHERE a.metricName = :metricName AND a.recordedDate BETWEEN :startDate AND :endDate AND a.countValue IS NOT NULL AND a.isActive = true")
    Long getTotalCountValue(@Param("metricName") String metricName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Analytics a WHERE a.metricType IN :metricTypes AND a.isActive = true ORDER BY a.recordedDate DESC")
    List<Analytics> findByMetricTypesIn(@Param("metricTypes") List<Analytics.MetricType> metricTypes);
}