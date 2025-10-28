package com.proposalmanagement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for assigning one or more reviewers to a proposal
 */
public class AssignReviewerRequest {
    
    @NotNull(message = "Proposal ID is required")
    private Long proposalId;
    
    @NotNull(message = "At least one reviewer ID is required")
    private List<Long> reviewerIds;
    
    private LocalDateTime dueDate;
    
    private String notes;
    
    // Constructors
    public AssignReviewerRequest() {}
    
    public AssignReviewerRequest(Long proposalId, List<Long> reviewerIds) {
        this.proposalId = proposalId;
        this.reviewerIds = reviewerIds;
    }
    
    // Getters and Setters
    public Long getProposalId() {
        return proposalId;
    }
    
    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }
    
    public List<Long> getReviewerIds() {
        return reviewerIds;
    }
    
    public void setReviewerIds(List<Long> reviewerIds) {
        this.reviewerIds = reviewerIds;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
