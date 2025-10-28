package com.proposalmanagement.controller;

import com.proposalmanagement.dto.AssignReviewerRequest;
import com.proposalmanagement.dto.ProposalReviewerDTO;
import com.proposalmanagement.entity.ProposalReviewer;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.service.ProposalReviewerService;
import com.proposalmanagement.service.UserService;
import com.proposalmanagement.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proposal-reviewers")
@CrossOrigin(origins = "*")
@Tag(name = "Proposal Reviewer Assignment", description = "APIs for managing proposal-reviewer assignments")
public class ProposalReviewerController {
    
    @Autowired
    private ProposalReviewerService proposalReviewerService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Assign reviewers to a proposal (COMMITTEE_CHAIR and ADMIN only)
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN')")
    @Operation(summary = "Assign reviewers to proposal", 
               description = "Committee Chair or Admin assigns one or more reviewers to a proposal")
    public ResponseEntity<List<ProposalReviewerDTO>> assignReviewers(
            @Valid @RequestBody AssignReviewerRequest request,
            HttpServletRequest httpRequest) {
        
        System.out.println("====== ASSIGN REVIEWERS ENDPOINT CALLED ======");
        System.out.println("Proposal ID: " + request.getProposalId());
        System.out.println("Reviewer IDs: " + request.getReviewerIds());
        System.out.println("Due Date: " + request.getDueDate());
        
        try {
            // Extract user ID from JWT
            Long assignedById = extractUserIdFromRequest(httpRequest);
            System.out.println("Assigned By ID: " + assignedById);
            
            // Assign reviewers
            List<ProposalReviewer> assignments = proposalReviewerService.assignReviewersToProposal(
                    request.getProposalId(),
                    request.getReviewerIds(),
                    assignedById,
                    request.getDueDate(),
                    request.getNotes()
            );
            
            // Convert to DTOs
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all assignments for a specific reviewer (REVIEWER role)
     */
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Get reviewer's assignments", 
               description = "Reviewer gets all proposals assigned to them")
    public ResponseEntity<List<ProposalReviewerDTO>> getMyAssignments(HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            
            List<ProposalReviewer> assignments = proposalReviewerService.getAssignmentsForReviewer(reviewerId);
            
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get pending assignments for current reviewer
     */
    @GetMapping("/my-assignments/pending")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Get pending assignments", 
               description = "Reviewer gets all pending assignments")
    public ResponseEntity<List<ProposalReviewerDTO>> getMyPendingAssignments(HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            
            List<ProposalReviewer> assignments = proposalReviewerService.getPendingAssignmentsForReviewer(reviewerId);
            
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get completed assignments for current reviewer
     */
    @GetMapping("/my-assignments/completed")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Get completed assignments", 
               description = "Reviewer gets all completed assignments")
    public ResponseEntity<List<ProposalReviewerDTO>> getMyCompletedAssignments(HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            
            List<ProposalReviewer> assignments = proposalReviewerService.getCompletedAssignmentsForReviewer(reviewerId);
            
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all reviewers assigned to a proposal (COMMITTEE_CHAIR, ADMIN, PI can view)
     */
    @GetMapping("/proposal/{proposalId}")
    @PreAuthorize("hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN') or hasRole('PRINCIPAL_INVESTIGATOR')")
    @Operation(summary = "Get reviewers for proposal", 
               description = "Get all reviewers assigned to a specific proposal")
    public ResponseEntity<List<ProposalReviewerDTO>> getReviewersForProposal(@PathVariable Long proposalId) {
        try {
            List<ProposalReviewer> assignments = proposalReviewerService.getReviewersForProposal(proposalId);
            
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check if current reviewer is assigned to a proposal
     */
    @GetMapping("/check-assignment/{proposalId}")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Check if assigned to proposal", 
               description = "Check if current reviewer is assigned to a specific proposal")
    public ResponseEntity<Boolean> checkAssignment(
            @PathVariable Long proposalId,
            HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            boolean isAssigned = proposalReviewerService.isReviewerAssignedToProposal(proposalId, reviewerId);
            return ResponseEntity.ok(isAssigned);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update assignment status
     */
    @PutMapping("/{assignmentId}/status")
    @PreAuthorize("hasRole('REVIEWER') or hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN')")
    @Operation(summary = "Update assignment status", 
               description = "Update the status of a proposal-reviewer assignment")
    public ResponseEntity<ProposalReviewerDTO> updateAssignmentStatus(
            @PathVariable Long assignmentId,
            @RequestParam ProposalReviewer.AssignmentStatus status) {
        try {
            ProposalReviewer updatedAssignment = proposalReviewerService.updateAssignmentStatus(assignmentId, status);
            return ResponseEntity.ok(new ProposalReviewerDTO(updatedAssignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Remove a reviewer assignment (COMMITTEE_CHAIR and ADMIN only)
     */
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN')")
    @Operation(summary = "Remove reviewer assignment", 
               description = "Remove a reviewer from a proposal assignment")
    public ResponseEntity<Void> removeAssignment(
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {
        try {
            Long requesterId = extractUserIdFromRequest(httpRequest);
            proposalReviewerService.removeReviewerAssignment(assignmentId, requesterId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get reviewer statistics
     */
    @GetMapping("/reviewer/{reviewerId}/statistics")
    @PreAuthorize("hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN') or hasRole('REVIEWER')")
    @Operation(summary = "Get reviewer statistics", 
               description = "Get assignment statistics for a reviewer")
    public ResponseEntity<ProposalReviewerService.ReviewerStatistics> getReviewerStatistics(
            @PathVariable Long reviewerId) {
        try {
            ProposalReviewerService.ReviewerStatistics stats = 
                    proposalReviewerService.getReviewerStatistics(reviewerId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get current reviewer's statistics
     */
    @GetMapping("/my-statistics")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Get my statistics", 
               description = "Get assignment statistics for current reviewer")
    public ResponseEntity<ProposalReviewerService.ReviewerStatistics> getMyStatistics(
            HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            ProposalReviewerService.ReviewerStatistics stats = 
                    proposalReviewerService.getReviewerStatistics(reviewerId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all overdue assignments (COMMITTEE_CHAIR and ADMIN only)
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('COMMITTEE_CHAIR') or hasRole('ADMIN')")
    @Operation(summary = "Get overdue assignments", 
               description = "Get all overdue proposal-reviewer assignments")
    public ResponseEntity<List<ProposalReviewerDTO>> getOverdueAssignments() {
        try {
            List<ProposalReviewer> assignments = proposalReviewerService.getOverdueAssignments();
            
            List<ProposalReviewerDTO> dtos = assignments.stream()
                    .map(ProposalReviewerDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get assigned proposal IDs for current reviewer
     */
    @GetMapping("/my-proposal-ids")
    @PreAuthorize("hasRole('REVIEWER')")
    @Operation(summary = "Get assigned proposal IDs", 
               description = "Get all proposal IDs assigned to current reviewer")
    public ResponseEntity<List<Long>> getMyAssignedProposalIds(HttpServletRequest httpRequest) {
        try {
            Long reviewerId = extractUserIdFromRequest(httpRequest);
            List<Long> proposalIds = proposalReviewerService.getAssignedProposalIds(reviewerId);
            return ResponseEntity.ok(proposalIds);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Helper method to extract user ID from JWT token
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No valid Authorization header found");
        }
        
        String jwtToken = authHeader.substring(7);
        return jwtUtil.extractUserId(jwtToken);
    }
}
