package com.proposalmanagement.dto;

import com.proposalmanagement.entity.ProposalReviewer;
import java.time.LocalDateTime;

/**
 * DTO for ProposalReviewer entity responses
 */
public class ProposalReviewerDTO {
    
    private Long id;
    private Long proposalId;
    private String proposalTitle;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedDate;
    private LocalDateTime dueDate;
    private ProposalReviewer.AssignmentStatus status;
    private String notes;
    private LocalDateTime completedDate;
    
    // Constructors
    public ProposalReviewerDTO() {}
    
    public ProposalReviewerDTO(ProposalReviewer proposalReviewer) {
        this.id = proposalReviewer.getId();
        this.proposalId = proposalReviewer.getProposal().getId();
        this.proposalTitle = proposalReviewer.getProposal().getTitle();
        this.reviewerId = proposalReviewer.getReviewer().getId();
        this.reviewerName = proposalReviewer.getReviewer().getFirstName() + " " + 
                           proposalReviewer.getReviewer().getLastName();
        this.reviewerEmail = proposalReviewer.getReviewer().getEmail();
        this.assignedById = proposalReviewer.getAssignedBy().getId();
        this.assignedByName = proposalReviewer.getAssignedBy().getFirstName() + " " + 
                             proposalReviewer.getAssignedBy().getLastName();
        this.assignedDate = proposalReviewer.getAssignedDate();
        this.dueDate = proposalReviewer.getDueDate();
        this.status = proposalReviewer.getStatus();
        this.notes = proposalReviewer.getNotes();
        this.completedDate = proposalReviewer.getCompletedDate();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProposalId() {
        return proposalId;
    }
    
    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }
    
    public String getProposalTitle() {
        return proposalTitle;
    }
    
    public void setProposalTitle(String proposalTitle) {
        this.proposalTitle = proposalTitle;
    }
    
    public Long getReviewerId() {
        return reviewerId;
    }
    
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
    
    public String getReviewerName() {
        return reviewerName;
    }
    
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }
    
    public String getReviewerEmail() {
        return reviewerEmail;
    }
    
    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }
    
    public Long getAssignedById() {
        return assignedById;
    }
    
    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }
    
    public String getAssignedByName() {
        return assignedByName;
    }
    
    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
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
    
    public ProposalReviewer.AssignmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProposalReviewer.AssignmentStatus status) {
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
