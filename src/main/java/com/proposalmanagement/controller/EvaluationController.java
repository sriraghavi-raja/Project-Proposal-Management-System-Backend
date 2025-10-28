package com.proposalmanagement.controller;

import com.proposalmanagement.dto.EvaluationDTO;
import com.proposalmanagement.entity.Evaluation;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.service.EvaluationService;
import com.proposalmanagement.service.ProposalService;
import com.proposalmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {
    
    @Autowired
    private EvaluationService evaluationService;
    
    @Autowired
    private ProposalService proposalService;
    
    @Autowired
    private UserService userService;
    
    // Get all evaluations (Admin, Committee Chair, Department Head can see all evaluations)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMITTEE_CHAIR') or hasRole('DEPARTMENT_HEAD')")
    public ResponseEntity<List<Evaluation>> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationService.getAllEvaluations();
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Evaluation> getEvaluationById(@PathVariable Long id) {
        Optional<Evaluation> evaluation = evaluationService.getEvaluationById(id);
        return evaluation.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    // Create evaluation (Reviewer, Committee Chair can create evaluations)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('REVIEWER') or hasRole('COMMITTEE_CHAIR')")
    public ResponseEntity<?> createEvaluation(@RequestBody EvaluationDTO evaluationDTO) {
        try {
            // Validate required fields
            if (evaluationDTO.getProposalId() == null) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("message", "Proposal ID is required"));
            }
            if (evaluationDTO.getReviewerId() == null) {
                return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("message", "Reviewer ID is required"));
            }
            
            // Convert DTO to entity
            Evaluation evaluation = new Evaluation();
            
            // Set proposal and reviewer by ID
            Proposal proposal = new Proposal();
            proposal.setId(evaluationDTO.getProposalId());
            evaluation.setProposal(proposal);
            
            User reviewer = new User();
            reviewer.setId(evaluationDTO.getReviewerId());
            evaluation.setReviewer(reviewer);
            
            // Set other properties
            evaluation.setEvaluationStage(evaluationDTO.getEvaluationStage());
            evaluation.setOverallScore(evaluationDTO.getOverallScore());
            evaluation.setTechnicalScore(evaluationDTO.getTechnicalScore());
            evaluation.setInnovationScore(evaluationDTO.getInnovationScore());
            evaluation.setFeasibilityScore(evaluationDTO.getFeasibilityScore());
            evaluation.setBudgetScore(evaluationDTO.getBudgetScore());
            evaluation.setImpactScore(evaluationDTO.getImpactScore());
            evaluation.setComments(evaluationDTO.getComments());
            evaluation.setRecommendation(evaluationDTO.getRecommendation());
            evaluation.setIsFinal(evaluationDTO.getIsFinal());
            evaluation.setConflictOfInterest(evaluationDTO.getConflictOfInterest());
            
            Evaluation createdEvaluation = evaluationService.createEvaluation(evaluation);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvaluation);
        } catch (RuntimeException e) {
            // Return a JSON error message to help frontend debugging
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("message", e.getMessage()));
        }
    }
    
    // Update evaluation (Reviewer who created it, Committee Chair, Admin can update)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('REVIEWER') or hasRole('COMMITTEE_CHAIR')")
    public ResponseEntity<Evaluation> updateEvaluation(@PathVariable Long id, @RequestBody EvaluationDTO evaluationDTO) {
        try {
            // Validate required fields
            if (evaluationDTO.getProposalId() == null) {
                return ResponseEntity.badRequest().build();
            }
            if (evaluationDTO.getReviewerId() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Convert DTO to entity
            Evaluation evaluationDetails = new Evaluation();
            
            // Set proposal and reviewer by ID
            Proposal proposal = new Proposal();
            proposal.setId(evaluationDTO.getProposalId());
            evaluationDetails.setProposal(proposal);
            
            User reviewer = new User();
            reviewer.setId(evaluationDTO.getReviewerId());
            evaluationDetails.setReviewer(reviewer);
            
            // Set other properties
            evaluationDetails.setEvaluationStage(evaluationDTO.getEvaluationStage());
            evaluationDetails.setOverallScore(evaluationDTO.getOverallScore());
            evaluationDetails.setTechnicalScore(evaluationDTO.getTechnicalScore());
            evaluationDetails.setInnovationScore(evaluationDTO.getInnovationScore());
            evaluationDetails.setFeasibilityScore(evaluationDTO.getFeasibilityScore());
            evaluationDetails.setBudgetScore(evaluationDTO.getBudgetScore());
            evaluationDetails.setImpactScore(evaluationDTO.getImpactScore());
            evaluationDetails.setComments(evaluationDTO.getComments());
            evaluationDetails.setRecommendation(evaluationDTO.getRecommendation());
            evaluationDetails.setIsFinal(evaluationDTO.getIsFinal());
            evaluationDetails.setConflictOfInterest(evaluationDTO.getConflictOfInterest());
            
            Evaluation updatedEvaluation = evaluationService.updateEvaluation(id, evaluationDetails);
            return ResponseEntity.ok(updatedEvaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        try {
            evaluationService.deleteEvaluation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<List<Evaluation>> getEvaluationsByProposalId(@PathVariable Long proposalId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByProposalId(proposalId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<Evaluation>> getEvaluationsByReviewerId(@PathVariable Long reviewerId) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByReviewerId(reviewerId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/proposal/{proposalId}/reviewer/{reviewerId}")
    public ResponseEntity<Evaluation> getEvaluationByProposalAndReviewer(
            @PathVariable Long proposalId, @PathVariable Long reviewerId) {
        Optional<Evaluation> evaluation = evaluationService.getEvaluationByProposalAndReviewer(proposalId, reviewerId);
        return evaluation.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/recommendation/{recommendation}")
    public ResponseEntity<List<Evaluation>> getEvaluationsByRecommendation(@PathVariable Evaluation.Recommendation recommendation) {
        List<Evaluation> evaluations = evaluationService.getEvaluationsByRecommendation(recommendation);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/final")
    public ResponseEntity<List<Evaluation>> getFinalEvaluations() {
        List<Evaluation> evaluations = evaluationService.getFinalEvaluations();
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/conflict-of-interest")
    public ResponseEntity<List<Evaluation>> getConflictOfInterestEvaluations() {
        List<Evaluation> evaluations = evaluationService.getConflictOfInterestEvaluations();
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/proposal/{proposalId}/final")
    public ResponseEntity<List<Evaluation>> getFinalEvaluationsByProposal(@PathVariable Long proposalId) {
        List<Evaluation> evaluations = evaluationService.getFinalEvaluationsByProposal(proposalId);
        return ResponseEntity.ok(evaluations);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/overall")
    public ResponseEntity<BigDecimal> getAverageOverallScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageOverallScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/technical")
    public ResponseEntity<BigDecimal> getAverageTechnicalScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageTechnicalScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/innovation")
    public ResponseEntity<BigDecimal> getAverageInnovationScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageInnovationScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/feasibility")
    public ResponseEntity<BigDecimal> getAverageFeasibilityScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageFeasibilityScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/budget")
    public ResponseEntity<BigDecimal> getAverageBudgetScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageBudgetScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/scores/impact")
    public ResponseEntity<BigDecimal> getAverageImpactScore(@PathVariable Long proposalId) {
        BigDecimal average = evaluationService.getAverageImpactScore(proposalId);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/proposal/{proposalId}/count")
    public ResponseEntity<Long> getEvaluationCountByProposal(@PathVariable Long proposalId) {
        Long count = evaluationService.getEvaluationCountByProposal(proposalId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/proposal/{proposalId}/count/final")
    public ResponseEntity<Long> getFinalEvaluationCountByProposal(@PathVariable Long proposalId) {
        Long count = evaluationService.getFinalEvaluationCountByProposal(proposalId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/reviewer/{reviewerId}/pending")
    public ResponseEntity<List<Evaluation>> getPendingEvaluationsByReviewer(@PathVariable Long reviewerId) {
        List<Evaluation> evaluations = evaluationService.getPendingEvaluationsByReviewer(reviewerId);
        return ResponseEntity.ok(evaluations);
    }
    
    @PutMapping("/{id}/finalize")
    public ResponseEntity<Evaluation> finalizeEvaluation(@PathVariable Long id) {
        try {
            Evaluation finalizedEvaluation = evaluationService.finalizeEvaluation(id);
            return ResponseEntity.ok(finalizedEvaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/unfinalize")
    public ResponseEntity<Evaluation> unfinalizeEvaluation(@PathVariable Long id) {
        try {
            Evaluation unfinalizedEvaluation = evaluationService.unfinalizeEvaluation(id);
            return ResponseEntity.ok(unfinalizedEvaluation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}