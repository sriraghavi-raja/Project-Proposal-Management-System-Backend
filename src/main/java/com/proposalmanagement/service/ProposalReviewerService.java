package com.proposalmanagement.service;

import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.ProposalReviewer;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.ProposalRepository;
import com.proposalmanagement.repository.ProposalReviewerRepository;
import com.proposalmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProposalReviewerService {
    
    @Autowired
    private ProposalReviewerRepository proposalReviewerRepository;
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Assign multiple reviewers to a proposal
     * Only COMMITTEE_CHAIR or ADMIN can perform this action
     */
    public List<ProposalReviewer> assignReviewersToProposal(
            Long proposalId, 
            List<Long> reviewerIds, 
            Long assignedById,
            LocalDateTime dueDate,
            String notes) {
        
        // Validate proposal exists
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found with ID: " + proposalId));
        
        // Validate proposal is in SUBMITTED status
        if (proposal.getStatus() != Proposal.Status.SUBMITTED && 
            proposal.getStatus() != Proposal.Status.UNDER_REVIEW) {
            throw new RuntimeException("Only SUBMITTED or UNDER_REVIEW proposals can be assigned to reviewers. Current status: " + proposal.getStatus());
        }
        
        // Validate assigner exists and has appropriate role
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigner not found with ID: " + assignedById));
        
        if (assignedBy.getRole() != User.Role.COMMITTEE_CHAIR && 
            assignedBy.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only COMMITTEE_CHAIR or ADMIN can assign reviewers");
        }
        
        List<ProposalReviewer> assignments = new ArrayList<>();
        
        for (Long reviewerId : reviewerIds) {
            // Validate reviewer exists and has REVIEWER role
            User reviewer = userRepository.findById(reviewerId)
                    .orElseThrow(() -> new RuntimeException("Reviewer not found with ID: " + reviewerId));
            
            if (reviewer.getRole() != User.Role.REVIEWER) {
                throw new RuntimeException("User " + reviewer.getUsername() + " is not a REVIEWER");
            }
            
            // Check if already assigned
            if (proposalReviewerRepository.existsByProposalIdAndReviewerId(proposalId, reviewerId)) {
                throw new RuntimeException("Reviewer " + reviewer.getUsername() + 
                                         " is already assigned to this proposal");
            }
            
            // Create assignment
            ProposalReviewer assignment = new ProposalReviewer(proposal, reviewer, assignedBy);
            assignment.setDueDate(dueDate);
            assignment.setNotes(notes);
            assignment.setStatus(ProposalReviewer.AssignmentStatus.PENDING);
            
            ProposalReviewer savedAssignment = proposalReviewerRepository.save(assignment);
            assignments.add(savedAssignment);
            
            // Send notification to reviewer
            try {
                System.out.println("=== REVIEWER ASSIGNMENT NOTIFICATION DEBUG ===");
                System.out.println("Reviewer: " + (reviewer != null ? reviewer.getUsername() : "NULL"));
                System.out.println("Proposal: " + (proposal != null ? proposal.getTitle() : "NULL"));
                
                String notificationTitle = "New Proposal Assignment";
                String notificationMessage = String.format(
                    "You have been assigned to review the proposal: '%s'%s",
                    proposal.getTitle(),
                    dueDate != null ? ". Due date: " + dueDate.toLocalDate().toString() : ""
                );
                
                System.out.println("Creating notification for Reviewer: " + reviewer.getUsername());
                System.out.println("Title: " + notificationTitle);
                System.out.println("Message: " + notificationMessage);
                
                com.proposalmanagement.entity.Notification notification = new com.proposalmanagement.entity.Notification();
                notification.setUser(reviewer);
                notification.setTitle(notificationTitle);
                notification.setMessage(notificationMessage);
                notification.setType(com.proposalmanagement.entity.Notification.Type.EVALUATION_ASSIGNED);
                notification.setRelatedProposal(proposal);
                notification.setIsRead(false);
                
                com.proposalmanagement.entity.Notification savedNotification = notificationService.createNotification(notification);
                System.out.println("Reviewer notification created successfully with ID: " + savedNotification.getId());
            } catch (Exception e) {
                // Log error but don't fail the assignment
                System.err.println("Failed to send notification to reviewer " + reviewer.getUsername() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Update proposal status to UNDER_REVIEW if it was SUBMITTED
        if (proposal.getStatus() == Proposal.Status.SUBMITTED) {
            proposal.setStatus(Proposal.Status.UNDER_REVIEW);
            proposalRepository.save(proposal);
        }
        
        return assignments;
    }
    
    /**
     * Get all proposals assigned to a specific reviewer
     */
    public List<ProposalReviewer> getAssignmentsForReviewer(Long reviewerId) {
        return proposalReviewerRepository.findByReviewerId(reviewerId);
    }
    
    /**
     * Get all reviewers assigned to a specific proposal
     */
    public List<ProposalReviewer> getReviewersForProposal(Long proposalId) {
        return proposalReviewerRepository.findByProposalId(proposalId);
    }
    
    /**
     * Check if a reviewer is assigned to a proposal
     */
    public boolean isReviewerAssignedToProposal(Long proposalId, Long reviewerId) {
        return proposalReviewerRepository.existsByProposalIdAndReviewerId(proposalId, reviewerId);
    }
    
    /**
     * Get pending assignments for a reviewer
     */
    public List<ProposalReviewer> getPendingAssignmentsForReviewer(Long reviewerId) {
        return proposalReviewerRepository.findPendingAssignmentsByReviewer(reviewerId);
    }
    
    /**
     * Get completed assignments for a reviewer
     */
    public List<ProposalReviewer> getCompletedAssignmentsForReviewer(Long reviewerId) {
        return proposalReviewerRepository.findCompletedAssignmentsByReviewer(reviewerId);
    }
    
    /**
     * Update assignment status
     */
    public ProposalReviewer updateAssignmentStatus(Long assignmentId, ProposalReviewer.AssignmentStatus status) {
        ProposalReviewer assignment = proposalReviewerRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + assignmentId));
        
        assignment.setStatus(status);
        
        if (status == ProposalReviewer.AssignmentStatus.COMPLETED) {
            assignment.setCompletedDate(LocalDateTime.now());
        }
        
        return proposalReviewerRepository.save(assignment);
    }
    
    /**
     * Update assignment status by proposal and reviewer
     */
    public Optional<ProposalReviewer> updateAssignmentStatusByProposalAndReviewer(
            Long proposalId,
            Long reviewerId,
            ProposalReviewer.AssignmentStatus status) {

        Optional<ProposalReviewer> assignmentOptional = proposalReviewerRepository
                .findByProposalIdAndReviewerId(proposalId, reviewerId);

        assignmentOptional.ifPresent(assignment -> {
            assignment.setStatus(status);
            if (status == ProposalReviewer.AssignmentStatus.COMPLETED) {
                assignment.setCompletedDate(LocalDateTime.now());
            }
            proposalReviewerRepository.save(assignment);
        });

        return assignmentOptional;
    }
    
    /**
     * Remove a reviewer assignment
     */
    public void removeReviewerAssignment(Long assignmentId, Long requesterId) {
        ProposalReviewer assignment = proposalReviewerRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + assignmentId));
        
        // Validate requester has permission (COMMITTEE_CHAIR or ADMIN)
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with ID: " + requesterId));
        
        if (requester.getRole() != User.Role.COMMITTEE_CHAIR && 
            requester.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only COMMITTEE_CHAIR or ADMIN can remove assignments");
        }
        
        proposalReviewerRepository.delete(assignment);
    }
    
    /**
     * Get all overdue assignments
     */
    public List<ProposalReviewer> getOverdueAssignments() {
        return proposalReviewerRepository.findOverdueAssignments(LocalDateTime.now());
    }
    
    /**
     * Get assignment statistics for a reviewer
     */
    public ReviewerStatistics getReviewerStatistics(Long reviewerId) {
        Long totalAssignments = proposalReviewerRepository.countByReviewerId(reviewerId);
        Long pendingAssignments = proposalReviewerRepository.countByReviewerIdAndStatus(
                reviewerId, ProposalReviewer.AssignmentStatus.PENDING);
        Long inProgressAssignments = proposalReviewerRepository.countByReviewerIdAndStatus(
                reviewerId, ProposalReviewer.AssignmentStatus.IN_PROGRESS);
        Long completedAssignments = proposalReviewerRepository.countByReviewerIdAndStatus(
                reviewerId, ProposalReviewer.AssignmentStatus.COMPLETED);
        
        return new ReviewerStatistics(totalAssignments, pendingAssignments, 
                                     inProgressAssignments, completedAssignments);
    }
    
    /**
     * Get proposal IDs assigned to a reviewer
     */
    public List<Long> getAssignedProposalIds(Long reviewerId) {
        return proposalReviewerRepository.findProposalIdsByReviewerId(reviewerId);
    }
    
    /**
     * Get assignment by ID
     */
    public Optional<ProposalReviewer> getAssignmentById(Long assignmentId) {
        return proposalReviewerRepository.findById(assignmentId);
    }
    
    /**
     * Inner class for reviewer statistics
     */
    public static class ReviewerStatistics {
        private Long totalAssignments;
        private Long pendingAssignments;
        private Long inProgressAssignments;
        private Long completedAssignments;
        
        public ReviewerStatistics(Long total, Long pending, Long inProgress, Long completed) {
            this.totalAssignments = total;
            this.pendingAssignments = pending;
            this.inProgressAssignments = inProgress;
            this.completedAssignments = completed;
        }
        
        // Getters
        public Long getTotalAssignments() { return totalAssignments; }
        public Long getPendingAssignments() { return pendingAssignments; }
        public Long getInProgressAssignments() { return inProgressAssignments; }
        public Long getCompletedAssignments() { return completedAssignments; }
    }
}
