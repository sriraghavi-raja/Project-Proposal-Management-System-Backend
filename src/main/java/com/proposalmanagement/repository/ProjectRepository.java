package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    Optional<Project> findByProposal(Proposal proposal);
    
    Optional<Project> findByProposalId(Long proposalId);
    
    Optional<Project> findByProjectNumber(String projectNumber);
    
    List<Project> findByStatus(Project.Status status);
    
    @Query("SELECT p FROM Project p WHERE p.startDate BETWEEN :startDate AND :endDate")
    List<Project> findByStartDateBetween(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :startDate AND :endDate")
    List<Project> findByEndDateBetween(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Project p WHERE p.endDate <= :date AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsEndingBefore(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Project p WHERE p.proposal.principalInvestigator.id = :piId")
    List<Project> findByPrincipalInvestigatorId(@Param("piId") Long piId);
    
    @Query("SELECT p FROM Project p WHERE p.proposal.departmentId = :departmentId")
    List<Project> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") Project.Status status);
    
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.proposal.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.projectNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Project> searchByTitleOrProjectNumber(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Project p WHERE p.completionPercentage < :percentage AND p.status = 'ACTIVE'")
    List<Project> findActiveProjectsWithLowCompletion(@Param("percentage") java.math.BigDecimal percentage);
}