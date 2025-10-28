package com.proposalmanagement.dto;

import com.proposalmanagement.entity.Evaluation;

import java.math.BigDecimal;

public class EvaluationDTO {
    
    private Long proposalId;
    private Long reviewerId;
    
    private String evaluationStage;
    private BigDecimal overallScore;
    private BigDecimal technicalScore;
    private BigDecimal innovationScore;
    private BigDecimal feasibilityScore;
    private BigDecimal budgetScore;
    private BigDecimal impactScore;
    private String comments;
    private Evaluation.Recommendation recommendation;
    private Boolean isFinal = false;
    private Boolean conflictOfInterest = false;
    
    // Constructors
    public EvaluationDTO() {}
    
    // Getters and Setters
    public Long getProposalId() {
        return proposalId;
    }
    
    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }
    
    public Long getReviewerId() {
        return reviewerId;
    }
    
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
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
    
    public Evaluation.Recommendation getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(Evaluation.Recommendation recommendation) {
        this.recommendation = recommendation;
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