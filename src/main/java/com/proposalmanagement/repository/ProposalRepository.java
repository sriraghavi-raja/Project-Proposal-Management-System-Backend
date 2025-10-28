package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    
    List<Proposal> findByPrincipalInvestigator(User principalInvestigator);
    
    List<Proposal> findByStatus(Proposal.Status status);
    
    List<Proposal> findByProjectType(Proposal.ProjectType projectType);
    
    List<Proposal> findByDepartmentId(Long departmentId);
    
    List<Proposal> findByCreatedBy(User createdBy);
    
    @Query("SELECT p FROM Proposal p WHERE p.submissionDeadline BETWEEN :startDate AND :endDate")
    List<Proposal> findBySubmissionDeadlineBetween(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
    
    @Query("SELECT p FROM Proposal p WHERE p.status = :status AND p.departmentId = :departmentId")
    List<Proposal> findByStatusAndDepartmentId(@Param("status") Proposal.Status status, 
                                              @Param("departmentId") Long departmentId);
    
    @Query("SELECT p FROM Proposal p WHERE p.principalInvestigator.id = :piId AND p.status = :status")
    List<Proposal> findByPrincipalInvestigatorIdAndStatus(@Param("piId") Long piId, 
                                                         @Param("status") Proposal.Status status);
    
    @Query("SELECT p FROM Proposal p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.abstract_) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Proposal> searchByTitleOrAbstract(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Proposal p WHERE p.requestedAmount >= :minAmount AND p.requestedAmount <= :maxAmount")
    List<Proposal> findByRequestedAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, 
                                               @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.status = :status")
    Long countByStatus(@Param("status") Proposal.Status status);
    
    @Query("SELECT p FROM Proposal p WHERE p.submissionDeadline <= :date AND p.status IN :statuses")
    List<Proposal> findUpcomingDeadlines(@Param("date") LocalDate date, 
                                        @Param("statuses") List<Proposal.Status> statuses);
    
    // Department-related queries
    @Query("SELECT COUNT(p) FROM Proposal p WHERE p.departmentId = :departmentId AND p.status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED')")
    Long countActiveProposalsByDepartmentId(@Param("departmentId") Long departmentId);
}