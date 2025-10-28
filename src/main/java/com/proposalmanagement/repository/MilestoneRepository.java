package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    
    List<Milestone> findByProjectIdOrderByDueDateAsc(Long projectId);
    
    List<Milestone> findByProjectIdAndStatusOrderByDueDateAsc(Long projectId, Milestone.Status status);
    
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.isActive = true ORDER BY m.dueDate ASC")
    List<Milestone> findActiveByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT m FROM Milestone m WHERE m.dueDate < :date AND m.status != :status AND m.isActive = true")
    List<Milestone> findOverdueMilestones(@Param("date") LocalDate date, @Param("status") Milestone.Status status);
    
    @Query("SELECT m FROM Milestone m WHERE m.dueDate BETWEEN :startDate AND :endDate AND m.isActive = true ORDER BY m.dueDate ASC")
    List<Milestone> findMilestonesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT m FROM Milestone m WHERE m.assignedTo.id = :userId AND m.status = :status AND m.isActive = true ORDER BY m.dueDate ASC")
    List<Milestone> findByAssignedToAndStatus(@Param("userId") Long userId, @Param("status") Milestone.Status status);
    
    @Query("SELECT COUNT(m) FROM Milestone m WHERE m.project.id = :projectId AND m.status = :status AND m.isActive = true")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Milestone.Status status);
    
    @Query("SELECT AVG(m.completionPercentage) FROM Milestone m WHERE m.project.id = :projectId AND m.isActive = true")
    Double getAverageCompletionPercentageByProject(@Param("projectId") Long projectId);
    
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.completionPercentage = 100.0 AND m.isActive = true")
    List<Milestone> findCompletedMilestonesByProject(@Param("projectId") Long projectId);
    
    Optional<Milestone> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT m FROM Milestone m WHERE m.dueDate = :dueDate AND m.status IN (:statuses) AND m.isActive = true")
    List<Milestone> findMilestonesDueOn(@Param("dueDate") LocalDate dueDate, @Param("statuses") List<Milestone.Status> statuses);
    
    @Query("SELECT m FROM Milestone m WHERE m.project.proposal.principalInvestigator.id = :userId AND m.isActive = true ORDER BY m.dueDate ASC")
    List<Milestone> findByPrincipalInvestigator(@Param("userId") Long userId);
}