package com.proposalmanagement.service;

import com.proposalmanagement.entity.AuditLog;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
    
    public Optional<AuditLog> getAuditLogById(Long id) {
        return auditLogRepository.findById(id);
    }
    
    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
    
    public void deleteAuditLog(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit log not found with id: " + id));
        auditLogRepository.delete(auditLog);
    }
    
    public List<AuditLog> getAuditLogsByUser(User user) {
        return auditLogRepository.findByUser(user);
    }
    
    public List<AuditLog> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
    
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
    
    public List<AuditLog> getAuditLogsByTable(String tableName) {
        return auditLogRepository.findByTableName(tableName);
    }
    
    public List<AuditLog> getAuditLogsByTableAndRecord(String tableName, Long recordId) {
        return auditLogRepository.findByTableNameAndRecordIdOrderByCreatedDateDesc(tableName, recordId);
    }
    
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedDateBetween(startDate, endDate);
    }
    
    public List<AuditLog> getAuditLogsByUserAndDateRange(Long userId, LocalDateTime startDate, 
                                                        LocalDateTime endDate) {
        return auditLogRepository.findByUserIdAndCreatedDateBetween(userId, startDate, endDate);
    }
    
    public Long getAuditLogCountByUser(Long userId) {
        return auditLogRepository.countByUserId(userId);
    }
    
    // Utility methods for creating audit logs
    public AuditLog logCreate(User user, String tableName, Long recordId, String newValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction("CREATE");
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setNewValues(newValues);
        
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog logUpdate(User user, String tableName, Long recordId, 
                             String oldValues, String newValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction("UPDATE");
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog logDelete(User user, String tableName, Long recordId, String oldValues) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction("DELETE");
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setOldValues(oldValues);
        
        return auditLogRepository.save(auditLog);
    }
    
    public AuditLog logCustomAction(User user, String action, String tableName, Long recordId, 
                                   String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setNewValues(description);
        
        return auditLogRepository.save(auditLog);
    }
    
    public void deleteOldAuditLogs(LocalDateTime beforeDate) {
        List<AuditLog> oldLogs = auditLogRepository.findByCreatedDateBetween(
            LocalDateTime.of(1900, 1, 1, 0, 0), beforeDate);
        auditLogRepository.deleteAll(oldLogs);
    }
    
    public void deleteAuditLogsByUser(Long userId) {
        List<AuditLog> userLogs = auditLogRepository.findByUserId(userId);
        auditLogRepository.deleteAll(userLogs);
    }
}