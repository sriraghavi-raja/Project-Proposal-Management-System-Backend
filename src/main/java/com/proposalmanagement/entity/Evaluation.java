package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proposal_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"evaluations", "documents", "budgetItems", "comments", "workflowStates", "project", "principalInvestigator"})
    private Proposal proposal;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"evaluations", "proposals", "password", "refreshToken"})
    private User reviewer;
    
    @Column(name = "evaluation_stage", length = 100)
    private String evaluationStage;
    
    @Column(name = "overall_score", precision = 4, scale = 2)
    private BigDecimal overallScore;
    
    @Column(name = "technical_score", precision = 4, scale = 2)
    private BigDecimal technicalScore;
    
    @Column(name = "innovation_score", precision = 4, scale = 2)
    private BigDecimal innovationScore;
    
    @Column(name = "feasibility_score", precision = 4, scale = 2)
    private BigDecimal feasibilityScore;
    
    @Column(name = "budget_score", precision = 4, scale = 2)
    private BigDecimal budgetScore;
    
    @Column(name = "impact_score", precision = 4, scale = 2)
    private BigDecimal impactScore;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    @Enumerated(EnumType.STRING)
    private Recommendation recommendation;
    
    @CreationTimestamp
    @Column(name = "evaluation_date", updatable = false)
    private LocalDateTime evaluationDate;
    
    @Column(name = "is_final", nullable = false)
    private Boolean isFinal = false;
    
    @Column(name = "conflict_of_interest", nullable = false)
    private Boolean conflictOfInterest = false;
    
    public enum Recommendation {
        APPROVE, REJECT, MINOR_REVISIONS, MAJOR_REVISIONS
    }
    
    // Constructors
    public Evaluation() {}
    
    public Evaluation(Proposal proposal, User reviewer) {
        this.proposal = proposal;
        this.reviewer = reviewer;
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
    
    public String getEvaluationStage() {
        return evaluationStage;
    }
    
    public void setEvaluationStage(String evaluationStage) {
        this.evaluationStage = evaluationStage;
    }
    
    public BigDecimal getOverallScore() {
        return overallScore;
    }
    
    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }
    
    public BigDecimal getTechnicalScore() {
        return technicalScore;
    }
    
    public void setTechnicalScore(BigDecimal technicalScore) {
        this.technicalScore = technicalScore;
    }
    
    public BigDecimal getInnovationScore() {
        return innovationScore;
    }
    
    public void setInnovationScore(BigDecimal innovationScore) {
        this.innovationScore = innovationScore;
    }
    
    public BigDecimal getFeasibilityScore() {
        return feasibilityScore;
    }
    
    public void setFeasibilityScore(BigDecimal feasibilityScore) {
        this.feasibilityScore = feasibilityScore;
    }
    
    public BigDecimal getBudgetScore() {
        return budgetScore;
    }
    
    public void setBudgetScore(BigDecimal budgetScore) {
        this.budgetScore = budgetScore;
    }
    
    public BigDecimal getImpactScore() {
        return impactScore;
    }
    
    public void setImpactScore(BigDecimal impactScore) {
        this.impactScore = impactScore;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Recommendation getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }
    
    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }
    
    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }
    
    public Boolean getIsFinal() {
        return isFinal;
    }
    
    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public Boolean getConflictOfInterest() {
        return conflictOfInterest;
    }
    
    public void setConflictOfInterest(Boolean conflictOfInterest) {
        this.conflictOfInterest = conflictOfInterest;
    }
}