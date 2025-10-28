package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Evaluation;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    List<Evaluation> findByProposal(Proposal proposal);
    
    List<Evaluation> findByProposalId(Long proposalId);
    
    List<Evaluation> findByReviewer(User reviewer);
    
    List<Evaluation> findByReviewerId(Long reviewerId);
    
    Optional<Evaluation> findByProposalIdAndReviewerId(Long proposalId, Long reviewerId);
    
    List<Evaluation> findByRecommendation(Evaluation.Recommendation recommendation);
    
    List<Evaluation> findByIsFinalTrue();
    
    List<Evaluation> findByConflictOfInterestTrue();
    
    @Query("SELECT e FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    List<Evaluation> findFinalEvaluationsByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.overallScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageOverallScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.technicalScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageTechnicalScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.innovationScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageInnovationScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.feasibilityScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageFeasibilityScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.budgetScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageBudgetScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT AVG(e.impactScore) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    BigDecimal getAverageImpactScoreByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.proposal.id = :proposalId")
    Long countByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.proposal.id = :proposalId AND e.isFinal = true")
    Long countFinalEvaluationsByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT e FROM Evaluation e WHERE e.reviewer.id = :reviewerId AND e.isFinal = false")
    List<Evaluation> findPendingEvaluationsByReviewerId(@Param("reviewerId") Long reviewerId);
    
    // Delete methods for cascade deletion
    void deleteByProposalId(Long proposalId);
    
    void deleteByProposal(Proposal proposal);
    
    void deleteByReviewerId(Long reviewerId);
}