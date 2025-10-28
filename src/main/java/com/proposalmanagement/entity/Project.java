package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    @JsonBackReference("proposal-project")
    private Proposal proposal;
    
    @Column(name = "project_number", length = 50, unique = true)
    private String projectNumber;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;
    
    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PLANNING;
    
    @Column(name = "completion_percentage", precision = 5, scale = 2)
    private BigDecimal completionPercentage = BigDecimal.ZERO;
    
    @Column(name = "budget_utilized", precision = 15, scale = 2)
    private BigDecimal budgetUtilized = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    // Relationships
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"project", "hibernateLazyInitializer", "handler"})
    private List<Milestone> milestones = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"project", "hibernateLazyInitializer", "handler"})
    private List<Document> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"project", "hibernateLazyInitializer", "handler"})
    private List<Comment> comments = new ArrayList<>();
    
    public enum Status {
        PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
    }
    
    // Constructors
    public Project() {}
    
    public Project(Proposal proposal, String projectNumber) {
        this.proposal = proposal;
        this.projectNumber = projectNumber;
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
    
    public String getProjectNumber() {
        return projectNumber;
    }
    
    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getActualStartDate() {
        return actualStartDate;
    }
    
    public void setActualStartDate(LocalDate actualStartDate) {
        this.actualStartDate = actualStartDate;
    }
    
    public LocalDate getActualEndDate() {
        return actualEndDate;
    }
    
    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public BigDecimal getBudgetUtilized() {
        return budgetUtilized;
    }
    
    public void setBudgetUtilized(BigDecimal budgetUtilized) {
        this.budgetUtilized = budgetUtilized;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    // Relationship getters and setters
    public List<Milestone> getMilestones() {
        return milestones;
    }
    
    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }
    
    public List<Document> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    // Helper methods
    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        milestone.setProject(this);
    }
    
    public void removeMilestone(Milestone milestone) {
        milestones.remove(milestone);
        milestone.setProject(null);
    }
    
    public void addDocument(Document document) {
        documents.add(document);
        document.setProject(this);
    }
    
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setProject(this);
    }
    
    // Helper method to get title from proposal
    public String getTitle() {
        return proposal != null ? proposal.getTitle() : null;
    }
}