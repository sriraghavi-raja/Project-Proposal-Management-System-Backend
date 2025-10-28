package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing the assignment of a reviewer to a proposal.
 * This enables the workflow where COMMITTEE_CHAIR assigns proposals to reviewers,
 * and reviewers can only access/evaluate assigned proposals.
 */
@Entity
@Table(name = "proposal_reviewers", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"proposal_id", "reviewer_id"}))
public class ProposalReviewer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy; // Committee Chair or Admin who made the assignment
    
    @CreationTimestamp
    @Column(name = "assigned_date", updatable = false)
    private LocalDateTime assignedDate;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Special instructions for the reviewer
    
    @Column(name = "completed_date")
    private LocalDateTime completedDate;
    
    public enum AssignmentStatus {
        PENDING,      // Assigned but not started
        IN_PROGRESS,  // Reviewer has started evaluation
        COMPLETED,    // Evaluation submitted
        OVERDUE       // Past due date without completion
    }
    
    // Constructors
    public ProposalReviewer() {}
    
    public ProposalReviewer(Proposal proposal, User reviewer, User assignedBy) {
        this.proposal = proposal;
        this.reviewer = reviewer;
        this.assignedBy = assignedBy;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Proposal getProposal() {
        return proposal;
    }
    
    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }
    
    public User getReviewer() {
        return reviewer;
    }
    
    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }
    
    public User getAssignedBy() {
        return assignedBy;
    }
    
    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }
    
    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }
    
    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
}
