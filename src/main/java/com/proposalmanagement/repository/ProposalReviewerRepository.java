package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.ProposalReviewer;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProposalReviewerRepository extends JpaRepository<ProposalReviewer, Long> {
    
    // Find all assignments for a specific reviewer
    List<ProposalReviewer> findByReviewer(User reviewer);
    
    // Find all assignments for a specific reviewer by ID
    List<ProposalReviewer> findByReviewerId(Long reviewerId);
    
    // Find all reviewers assigned to a specific proposal
    List<ProposalReviewer> findByProposal(Proposal proposal);
    
    // Find all reviewers assigned to a specific proposal by ID
    List<ProposalReviewer> findByProposalId(Long proposalId);
    
    // Check if a specific reviewer is assigned to a specific proposal
    Optional<ProposalReviewer> findByProposalIdAndReviewerId(Long proposalId, Long reviewerId);
    
    // Find assignments by status
    List<ProposalReviewer> findByStatus(ProposalReviewer.AssignmentStatus status);
    
    // Find assignments for a reviewer with specific status
    List<ProposalReviewer> findByReviewerIdAndStatus(Long reviewerId, ProposalReviewer.AssignmentStatus status);
    
    // Find all assignments made by a specific user (Committee Chair/Admin)
    List<ProposalReviewer> findByAssignedBy(User assignedBy);
    
    List<ProposalReviewer> findByAssignedById(Long assignedById);
    
    // Find overdue assignments
    @Query("SELECT pr FROM ProposalReviewer pr WHERE pr.dueDate < :currentDate AND pr.status != 'COMPLETED'")
    List<ProposalReviewer> findOverdueAssignments(@Param("currentDate") LocalDateTime currentDate);
    
    // Find pending assignments for a reviewer
    @Query("SELECT pr FROM ProposalReviewer pr WHERE pr.reviewer.id = :reviewerId AND pr.status = 'PENDING'")
    List<ProposalReviewer> findPendingAssignmentsByReviewer(@Param("reviewerId") Long reviewerId);
    
    // Find completed assignments for a reviewer
    @Query("SELECT pr FROM ProposalReviewer pr WHERE pr.reviewer.id = :reviewerId AND pr.status = 'COMPLETED'")
    List<ProposalReviewer> findCompletedAssignmentsByReviewer(@Param("reviewerId") Long reviewerId);
    
    // Count assignments for a reviewer
    Long countByReviewerId(Long reviewerId);
    
    // Count pending assignments for a reviewer
    Long countByReviewerIdAndStatus(Long reviewerId, ProposalReviewer.AssignmentStatus status);
    
    // Check if assignment already exists
    boolean existsByProposalIdAndReviewerId(Long proposalId, Long reviewerId);
    
    // Find assignments with due date in range
    @Query("SELECT pr FROM ProposalReviewer pr WHERE pr.dueDate BETWEEN :startDate AND :endDate")
    List<ProposalReviewer> findByDueDateBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get all proposals assigned to a reviewer (returns proposal IDs)
    @Query("SELECT pr.proposal.id FROM ProposalReviewer pr WHERE pr.reviewer.id = :reviewerId")
    List<Long> findProposalIdsByReviewerId(@Param("reviewerId") Long reviewerId);
}
