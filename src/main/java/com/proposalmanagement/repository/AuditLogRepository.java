package com.proposalmanagement.repository;

import com.proposalmanagement.entity.AuditLog;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByTableName(String tableName);
    
    List<AuditLog> findByTableNameAndRecordId(String tableName, Long recordId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.createdDate BETWEEN :startDate AND :endDate ORDER BY a.createdDate DESC")
    List<AuditLog> findByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.createdDate BETWEEN :startDate AND :endDate ORDER BY a.createdDate DESC")
    List<AuditLog> findByUserIdAndCreatedDateBetween(@Param("userId") Long userId, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.tableName = :tableName AND a.recordId = :recordId ORDER BY a.createdDate DESC")
    List<AuditLog> findByTableNameAndRecordIdOrderByCreatedDateDesc(@Param("tableName") String tableName, 
                                                                   @Param("recordId") Long recordId);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}