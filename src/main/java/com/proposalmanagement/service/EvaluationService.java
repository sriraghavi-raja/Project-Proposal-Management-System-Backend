package com.proposalmanagement.service;

import com.proposalmanagement.entity.Evaluation;
import com.proposalmanagement.entity.Notification;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.EvaluationRepository;
import com.proposalmanagement.repository.ProposalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EvaluationService {
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private ProposalReviewerService proposalReviewerService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private com.proposalmanagement.repository.UserRepository userRepository;
    
    public List<Evaluation> getAllEvaluations() {
        return evaluationRepository.findAll();
    }
    
    public Optional<Evaluation> getEvaluationById(Long id) {
        return evaluationRepository.findById(id);
    }
    
    public Evaluation createEvaluation(Evaluation evaluation) {
        // Basic validation: proposal and reviewer must be supplied with IDs
        if (evaluation.getProposal() == null || evaluation.getProposal().getId() == null) {
            throw new RuntimeException("Proposal id is required");
        }
        if (evaluation.getReviewer() == null || evaluation.getReviewer().getId() == null) {
            throw new RuntimeException("Reviewer id is required");
        }

        // Load managed Proposal and User entities to avoid transient/detached instance issues
        Long proposalId = evaluation.getProposal().getId();
        Long reviewerId = evaluation.getReviewer().getId();

        Proposal managedProposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + proposalId));

        User managedReviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found with id: " + reviewerId));

        // Create new evaluation instance to avoid serialization issues
        Evaluation newEvaluation = new Evaluation();
        newEvaluation.setProposal(managedProposal);
        newEvaluation.setReviewer(managedReviewer);
        newEvaluation.setEvaluationStage(evaluation.getEvaluationStage());
        newEvaluation.setTechnicalScore(evaluation.getTechnicalScore());
        newEvaluation.setInnovationScore(evaluation.getInnovationScore());
        newEvaluation.setFeasibilityScore(evaluation.getFeasibilityScore());
        newEvaluation.setBudgetScore(evaluation.getBudgetScore());
        newEvaluation.setImpactScore(evaluation.getImpactScore());
        newEvaluation.setOverallScore(evaluation.getOverallScore());
        newEvaluation.setComments(evaluation.getComments());
        newEvaluation.setRecommendation(evaluation.getRecommendation());
        newEvaluation.setConflictOfInterest(evaluation.getConflictOfInterest());
        newEvaluation.setIsFinal(evaluation.getIsFinal());

        // Check if evaluation already exists for this proposal and reviewer
        Optional<Evaluation> existingEvaluation = evaluationRepository
                .findByProposalIdAndReviewerId(
                    proposalId,
                    reviewerId
                );
        
        if (existingEvaluation.isPresent()) {
            throw new RuntimeException("Evaluation already exists for this proposal and reviewer");
        }
        
        // Set default values
        if (newEvaluation.getIsFinal() == null) {
            newEvaluation.setIsFinal(false);
        }
        if (newEvaluation.getConflictOfInterest() == null) {
            newEvaluation.setConflictOfInterest(false);
        }
        
        // Save the evaluation
        Evaluation savedEvaluation = evaluationRepository.save(newEvaluation);

        // Update proposal status based on recommendation
        updateProposalStatusBasedOnRecommendation(savedEvaluation);
        
        // Skip notification creation to avoid transaction issues
        System.out.println("Evaluation created successfully - skipping notification");
        
        // Skip assignment status update to avoid transaction issues
        System.out.println("Skipping assignment status update");
        
        return savedEvaluation;
    }
    
    private String getRecommendationText(Evaluation.Recommendation recommendation) {
        if (recommendation == null) return "Pending";
        switch (recommendation) {
            case APPROVE:
                return "Approved";
            case REJECT:
                return "Rejected";
            case MINOR_REVISIONS:
                return "Minor Revisions Required";
            case MAJOR_REVISIONS:
                return "Major Revisions Required";
            default:
                return "Pending";
        }
    }
    
    public Evaluation updateEvaluation(Long id, Evaluation evaluationDetails) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + id));
        
        evaluation.setEvaluationStage(evaluationDetails.getEvaluationStage());
        evaluation.setOverallScore(evaluationDetails.getOverallScore());
        evaluation.setTechnicalScore(evaluationDetails.getTechnicalScore());
        evaluation.setInnovationScore(evaluationDetails.getInnovationScore());
        evaluation.setFeasibilityScore(evaluationDetails.getFeasibilityScore());
        evaluation.setBudgetScore(evaluationDetails.getBudgetScore());
        evaluation.setImpactScore(evaluationDetails.getImpactScore());
        evaluation.setComments(evaluationDetails.getComments());
        evaluation.setRecommendation(evaluationDetails.getRecommendation());
        evaluation.setIsFinal(evaluationDetails.getIsFinal());
        evaluation.setConflictOfInterest(evaluationDetails.getConflictOfInterest());
        
        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        
        // Update proposal status based on recommendation
        updateProposalStatusBasedOnRecommendation(savedEvaluation);
        
        return savedEvaluation;
    }
    
    public void deleteEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + id));
        evaluationRepository.delete(evaluation);
    }
    
    public List<Evaluation> getEvaluationsByProposal(Proposal proposal) {
        return evaluationRepository.findByProposal(proposal);
    }
    
    public List<Evaluation> getEvaluationsByProposalId(Long proposalId) {
        return evaluationRepository.findByProposalId(proposalId);
    }
    
    public List<Evaluation> getEvaluationsByReviewer(User reviewer) {
        return evaluationRepository.findByReviewer(reviewer);
    }
    
    public List<Evaluation> getEvaluationsByReviewerId(Long reviewerId) {
        return evaluationRepository.findByReviewerId(reviewerId);
    }
    
    public Optional<Evaluation> getEvaluationByProposalAndReviewer(Long proposalId, Long reviewerId) {
        return evaluationRepository.findByProposalIdAndReviewerId(proposalId, reviewerId);
    }
    
    public List<Evaluation> getEvaluationsByRecommendation(Evaluation.Recommendation recommendation) {
        return evaluationRepository.findByRecommendation(recommendation);
    }
    
    public List<Evaluation> getFinalEvaluations() {
        return evaluationRepository.findByIsFinalTrue();
    }
    
    public List<Evaluation> getConflictOfInterestEvaluations() {
        return evaluationRepository.findByConflictOfInterestTrue();
    }
    
    public List<Evaluation> getFinalEvaluationsByProposal(Long proposalId) {
        return evaluationRepository.findFinalEvaluationsByProposalId(proposalId);
    }
    
    private void updateProposalStatusBasedOnRecommendation(Evaluation evaluation) {
        if (evaluation.getRecommendation() == Evaluation.Recommendation.APPROVE) {
            Proposal proposal = evaluation.getProposal();
            proposal.setStatus(Proposal.Status.APPROVED);
            proposalRepository.save(proposal);
        }
    }
    
    public BigDecimal getAverageOverallScore(Long proposalId) {
        return evaluationRepository.getAverageOverallScoreByProposalId(proposalId);
    }
    
    public BigDecimal getAverageTechnicalScore(Long proposalId) {
        return evaluationRepository.getAverageTechnicalScoreByProposalId(proposalId);
    }
    
    public BigDecimal getAverageInnovationScore(Long proposalId) {
        return evaluationRepository.getAverageInnovationScoreByProposalId(proposalId);
    }
    
    public BigDecimal getAverageFeasibilityScore(Long proposalId) {
        return evaluationRepository.getAverageFeasibilityScoreByProposalId(proposalId);
    }
    
    public BigDecimal getAverageBudgetScore(Long proposalId) {
        return evaluationRepository.getAverageBudgetScoreByProposalId(proposalId);
    }
    
    public BigDecimal getAverageImpactScore(Long proposalId) {
        return evaluationRepository.getAverageImpactScoreByProposalId(proposalId);
    }
    
    public Long getEvaluationCountByProposal(Long proposalId) {
        return evaluationRepository.countByProposalId(proposalId);
    }
    
    public Long getFinalEvaluationCountByProposal(Long proposalId) {
        return evaluationRepository.countFinalEvaluationsByProposalId(proposalId);
    }
    
    public List<Evaluation> getPendingEvaluationsByReviewer(Long reviewerId) {
        return evaluationRepository.findPendingEvaluationsByReviewerId(reviewerId);
    }
    
    public Evaluation finalizeEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + id));
        
        if (evaluation.getIsFinal()) {
            throw new RuntimeException("Evaluation is already finalized");
        }
        
        evaluation.setIsFinal(true);
        return evaluationRepository.save(evaluation);
    }
    
    public Evaluation unfinalizeEvaluation(Long id) {
        Evaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found with id: " + id));
        
        evaluation.setIsFinal(false);
        return evaluationRepository.save(evaluation);
    }
}