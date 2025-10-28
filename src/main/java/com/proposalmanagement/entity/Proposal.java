package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proposals")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Proposal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String abstract_;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "principal_investigator_id", nullable = false)
    private User principalInvestigator;
    
    @Column(name = "co_investigators", columnDefinition = "TEXT")
    private String coInvestigators;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    private ProjectType projectType;
    
    @Size(max = 200)
    @Column(name = "funding_agency")
    private String fundingAgency;
    
    @Column(name = "requested_amount", precision = 15, scale = 2)
    private BigDecimal requestedAmount;
    
    @Column(name = "project_duration_months")
    private Integer projectDurationMonths;
    
    @Column(name = "submission_deadline")
    private LocalDate submissionDeadline;
    
    @CreationTimestamp
    @Column(name = "submission_date", updatable = false)
    private LocalDateTime submissionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;
    
    @Size(max = 100)
    @Column(name = "project_stage")
    private String projectStage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;
    
    @NotNull
    @Column(name = "department_id", nullable = false)
    private Long departmentId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @UpdateTimestamp
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
    
    // Relationships
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proposal-documents")
    private List<Document> documents = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"proposal"})
    private List<Evaluation> evaluations = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proposal-budgetItems")
    private List<BudgetItem> budgetItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proposal-comments")
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proposal-workflowStates")
    private List<WorkflowState> workflowStates = new ArrayList<>();
    
    @OneToOne(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("proposal-project")
    private Project project;
    
    public enum ProjectType {
        RESEARCH, DEVELOPMENT, EDUCATION, SERVICE
    }
    
    public enum Status {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, WITHDRAWN
    }
    
    public enum PriorityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    // Constructors
    public Proposal() {}
    
    public Proposal(String title, User principalInvestigator, ProjectType projectType) {
        this.title = title;
        this.principalInvestigator = principalInvestigator;
        this.projectType = projectType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAbstract_() {
        return abstract_;
    }
    
    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }
    
    public User getPrincipalInvestigator() {
        return principalInvestigator;
    }
    
    public void setPrincipalInvestigator(User principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }
    
    public String getCoInvestigators() {
        return coInvestigators;
    }
    
    public void setCoInvestigators(String coInvestigators) {
        this.coInvestigators = coInvestigators;
    }
    
    public ProjectType getProjectType() {
        return projectType;
    }
    
    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }
    
    public String getFundingAgency() {
        return fundingAgency;
    }
    
    public void setFundingAgency(String fundingAgency) {
        this.fundingAgency = fundingAgency;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
    
    public Integer getProjectDurationMonths() {
        return projectDurationMonths;
    }
    
    public void setProjectDurationMonths(Integer projectDurationMonths) {
        this.projectDurationMonths = projectDurationMonths;
    }
    
    public LocalDate getSubmissionDeadline() {
        return submissionDeadline;
    }
    
    public void setSubmissionDeadline(LocalDate submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
    }
    
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getProjectStage() {
        return projectStage;
    }
    
    public void setProjectStage(String projectStage) {
        this.projectStage = projectStage;
    }
    
    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    // Relationship getters and setters
    public List<Document> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public List<Evaluation> getEvaluations() {
        return evaluations;
    }
    
    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }
    
    public List<BudgetItem> getBudgetItems() {
        return budgetItems;
    }
    
    public void setBudgetItems(List<BudgetItem> budgetItems) {
        this.budgetItems = budgetItems;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public List<WorkflowState> getWorkflowStates() {
        return workflowStates;
    }
    
    public void setWorkflowStates(List<WorkflowState> workflowStates) {
        this.workflowStates = workflowStates;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    // Helper methods
    public void addDocument(Document document) {
        documents.add(document);
        document.setProposal(this);
    }
    
    public void removeDocument(Document document) {
        documents.remove(document);
        document.setProposal(null);
    }
    
    public void addEvaluation(Evaluation evaluation) {
        evaluations.add(evaluation);
        evaluation.setProposal(this);
    }
    
    public void addBudgetItem(BudgetItem budgetItem) {
        budgetItems.add(budgetItem);
        budgetItem.setProposal(this);
    }
    
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setProposal(this);
    }
    
    public void addWorkflowState(WorkflowState workflowState) {
        workflowStates.add(workflowState);
        workflowState.setProposal(this);
    }
}