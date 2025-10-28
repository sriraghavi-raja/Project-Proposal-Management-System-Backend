package com.proposalmanagement.repository;

import com.proposalmanagement.entity.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {
    
    List<WorkflowState> findByProposalIdOrderByEntryDateDesc(Long proposalId);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.proposal.id = :proposalId AND ws.isActive = true ORDER BY ws.entryDate DESC")
    List<WorkflowState> findActiveByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.proposal.id = :proposalId AND ws.isActive = true ORDER BY ws.entryDate DESC LIMIT 1")
    Optional<WorkflowState> findCurrentStateByProposal(@Param("proposalId") Long proposalId);
    
    List<WorkflowState> findByStageAndStatusOrderByEntryDateDesc(String stage, WorkflowState.Status status);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.assignedTo.id = :userId AND ws.status = :status AND ws.isActive = true ORDER BY ws.entryDate ASC")
    List<WorkflowState> findByAssignedToAndStatus(@Param("userId") Long userId, @Param("status") WorkflowState.Status status);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.assignedTo.id = :userId AND ws.status = 'PENDING' AND ws.isActive = true ORDER BY ws.entryDate ASC")
    List<WorkflowState> findPendingTasksByUser(@Param("userId") Long userId);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.dueDate < :currentDateTime AND ws.status = 'PENDING' AND ws.isActive = true")
    List<WorkflowState> findOverdueTasks(@Param("currentDateTime") LocalDateTime currentDateTime);
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.dueDate BETWEEN :startDate AND :endDate AND ws.status = 'PENDING' AND ws.isActive = true ORDER BY ws.dueDate ASC")
    List<WorkflowState> findTasksDueInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(ws) FROM WorkflowState ws WHERE ws.assignedTo.id = :userId AND ws.status = :status AND ws.isActive = true")
    Long countByAssignedToAndStatus(@Param("userId") Long userId, @Param("status") WorkflowState.Status status);
    
    @Query("SELECT ws.stage, COUNT(ws) FROM WorkflowState ws WHERE ws.status = 'PENDING' AND ws.isActive = true GROUP BY ws.stage")
    List<Object[]> getWorkflowStageDistribution();
    
    @Query("SELECT ws FROM WorkflowState ws WHERE ws.proposal.id = :proposalId AND ws.stage = :stage AND ws.isActive = true ORDER BY ws.entryDate DESC")
    List<WorkflowState> findByProposalIdAndStage(@Param("proposalId") Long proposalId, @Param("stage") String stage);
    
    Optional<WorkflowState> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, ws.entryDate, ws.completedDate)) FROM WorkflowState ws WHERE ws.stage = :stage AND ws.status = 'COMPLETED' AND ws.completedDate IS NOT NULL")
    Double getAverageCompletionTimeHoursByStage(@Param("stage") String stage);
}