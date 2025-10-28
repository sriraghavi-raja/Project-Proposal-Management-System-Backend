package com.proposalmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "analytics")
public class Analytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;
    
    @NotNull
    @Column(name = "metric_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetricType metricType;
    
    @Column(name = "metric_value")
    private BigDecimal metricValue;
    
    @Column(name = "string_value", length = 500)
    private String stringValue;
    
    @Column(name = "count_value")
    private Long countValue;
    
    @Column(name = "percentage_value")
    private BigDecimal percentageValue;
    
    @Column(name = "entity_type", length = 50)
    private String entityType; // proposal, project, user, evaluation, etc.
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON format for additional data
    
    @CreationTimestamp
    @Column(name = "recorded_date", updatable = false)
    private LocalDateTime recordedDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    public enum MetricType {
        // Proposal metrics
        PROPOSAL_COUNT, PROPOSAL_APPROVAL_RATE, PROPOSAL_SUBMISSION_RATE,
        PROPOSAL_AVERAGE_REVIEW_TIME, PROPOSAL_SUCCESS_RATE_BY_DEPARTMENT,
        
        // Project metrics
        PROJECT_COUNT, PROJECT_COMPLETION_RATE, PROJECT_ON_TIME_COMPLETION,
        PROJECT_BUDGET_UTILIZATION, PROJECT_MILESTONE_COMPLETION_RATE,
        
        // User metrics
        USER_ACTIVITY, USER_LOGIN_FREQUENCY, EVALUATOR_WORKLOAD,
        USER_PROPOSAL_SUCCESS_RATE, USER_AVERAGE_EVALUATION_TIME,
        
        // System metrics
        SYSTEM_RESPONSE_TIME, DATABASE_PERFORMANCE, API_CALL_COUNT,
        ERROR_RATE, UPTIME_PERCENTAGE,
        
        // Financial metrics
        TOTAL_BUDGET_ALLOCATED, AVERAGE_PROJECT_COST, BUDGET_VARIANCE,
        COST_PER_PROPOSAL, DEPARTMENT_BUDGET_UTILIZATION,
        
        // Workflow metrics
        WORKFLOW_EFFICIENCY, APPROVAL_BOTTLENECKS, AVERAGE_WORKFLOW_TIME,
        WORKFLOW_STATE_DISTRIBUTION, ESCALATION_RATE,
        
        // Document metrics
        DOCUMENT_UPLOAD_COUNT, DOCUMENT_REVIEW_TIME, DOCUMENT_COMPLIANCE_RATE
    }
    
    // Constructors
    public Analytics() {}
    
    public Analytics(String metricName, MetricType metricType) {
        this.metricName = metricName;
        this.metricType = metricType;
    }
    
    public Analytics(String metricName, MetricType metricType, BigDecimal metricValue) {
        this.metricName = metricName;
        this.metricType = metricType;
        this.metricValue = metricValue;
    }
    
    public Analytics(String metricName, MetricType metricType, Long countValue) {
        this.metricName = metricName;
        this.metricType = metricType;
        this.countValue = countValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMetricName() {
        return metricName;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    public MetricType getMetricType() {
        return metricType;
    }
    
    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }
    
    public BigDecimal getMetricValue() {
        return metricValue;
    }
    
    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }
    
    public String getStringValue() {
        return stringValue;
    }
    
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    
    public Long getCountValue() {
        return countValue;
    }
    
    public void setCountValue(Long countValue) {
        this.countValue = countValue;
    }
    
    public BigDecimal getPercentageValue() {
        return percentageValue;
    }
    
    public void setPercentageValue(BigDecimal percentageValue) {
        this.percentageValue = percentageValue;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getPeriodStart() {
        return periodStart;
    }
    
    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }
    
    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }
    
    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getRecordedDate() {
        return recordedDate;
    }
    
    public void setRecordedDate(LocalDateTime recordedDate) {
        this.recordedDate = recordedDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}