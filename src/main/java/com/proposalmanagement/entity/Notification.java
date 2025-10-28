package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "passwordHash", "passwordResetToken", "emailVerificationToken", "twoFactorSecret"})
    private User user;
    
    @NotNull
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private Type type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent = false;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_proposal_id")
    @JsonIgnoreProperties({"principalInvestigator", "department", "createdBy"})
    private Proposal relatedProposal;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_project_id")
    @JsonIgnoreProperties({"proposal"})
    private Project relatedProject;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON format for additional data
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "read_date")
    private LocalDateTime readDate;
    
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    public enum Type {
        PROPOSAL_SUBMITTED, PROPOSAL_APPROVED, PROPOSAL_REJECTED, PROPOSAL_RETURNED,
        EVALUATION_ASSIGNED, EVALUATION_COMPLETED, EVALUATION_OVERDUE, EVALUATION_RECEIVED,
        PROJECT_CREATED, PROJECT_UPDATED, PROJECT_MILESTONE_ACHIEVED, PROJECT_MILESTONE_OVERDUE,
        BUDGET_APPROVED, BUDGET_REJECTED, BUDGET_UPDATED,
        DEADLINE_REMINDER, DEADLINE_APPROACHING, DEADLINE_MISSED,
        WORKFLOW_STATE_CHANGED, APPROVAL_REQUIRED,
        DOCUMENT_UPLOADED, DOCUMENT_REVIEWED,
        USER_ASSIGNED, USER_UNASSIGNED,
        SYSTEM_MAINTENANCE, GENERAL,
        INFO, WARNING, SUCCESS, ERROR, REMINDER
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    // Constructors
    public Notification() {}
    
    public Notification(User user, String title, String message, Type type) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public Proposal getRelatedProposal() {
        return relatedProposal;
    }
    
    public void setRelatedProposal(Proposal relatedProposal) {
        this.relatedProposal = relatedProposal;
    }
    
    public Project getRelatedProject() {
        return relatedProject;
    }
    
    public void setRelatedProject(Project relatedProject) {
        this.relatedProject = relatedProject;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getReadDate() {
        return readDate;
    }
    
    public void setReadDate(LocalDateTime readDate) {
        this.readDate = readDate;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Boolean getIsEmailSent() {
        return isEmailSent;
    }
    
    public void setIsEmailSent(Boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }
    
    public Long getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public String getRelatedEntityType() {
        return relatedEntityType;
    }
    
    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readDate = LocalDateTime.now();
    }
}