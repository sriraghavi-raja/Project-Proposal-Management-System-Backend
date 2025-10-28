package com.proposalmanagement.controller;

import com.proposalmanagement.dto.ProposalDTO;
import com.proposalmanagement.entity.Document;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.service.DocumentService;
import com.proposalmanagement.service.ProposalService;
import com.proposalmanagement.service.UserService;
import com.proposalmanagement.service.ProposalReviewerService;
import com.proposalmanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proposals")
@CrossOrigin(origins = "*")
public class ProposalController {
    
    @Autowired
    private ProposalService proposalService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProposalReviewerService proposalReviewerService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private Validator validator;
    
    @Autowired
    private DocumentService documentService;
    
    @Value("${file.upload-dir:uploads/documents}")
    private String uploadDir;
    
    // Get all proposals (Admin, Committee Chair, Department Head, Financial Officer can see all)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMITTEE_CHAIR') or hasRole('DEPARTMENT_HEAD') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE')")
    public ResponseEntity<List<ProposalDTO>> getAllProposals() {
        List<Proposal> proposals = proposalService.getAllProposals();
        List<ProposalDTO> proposalDTOs = proposals.stream()
                .map(ProposalDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(proposalDTOs);
    }
    
    // Get proposal by ID (All roles can view if authorized for specific proposal)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMITTEE_CHAIR') or hasRole('DEPARTMENT_HEAD') or " +
                  "hasRole('PROJECT_MANAGER') or hasRole('PRINCIPAL_INVESTIGATOR') or " +
                  "hasRole('REVIEWER') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE') or hasRole('STAKEHOLDER')")
    public ResponseEntity<ProposalDTO> getProposalById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Extract user from JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                Long userId = jwtUtil.extractUserId(jwtToken);
                User user = userService.getUserById(userId).orElse(null);
                
                // If user is a REVIEWER, check if they are assigned to this proposal
                if (user != null && user.getRole() == User.Role.REVIEWER) {
                    boolean isAssigned = proposalReviewerService.isReviewerAssignedToProposal(id, userId);
                    if (!isAssigned) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
            }
            
            Optional<Proposal> proposal = proposalService.getProposalById(id);
            return proposal.map(p -> ResponseEntity.ok(new ProposalDTO(p)))
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get proposals assigned to current reviewer (REVIEWER only)
    @GetMapping("/my-assigned-proposals")
    @PreAuthorize("hasRole('REVIEWER')")
    public ResponseEntity<List<ProposalDTO>> getMyAssignedProposals(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String jwtToken = authHeader.substring(7);
            Long reviewerId = jwtUtil.extractUserId(jwtToken);
            
            // Get assigned proposal IDs
            List<Long> assignedProposalIds = proposalReviewerService.getAssignedProposalIds(reviewerId);
            
            // Get proposals by IDs
            List<Proposal> proposals = proposalService.getAllProposals().stream()
                    .filter(p -> assignedProposalIds.contains(p.getId()))
                    .collect(Collectors.toList());
            
            List<ProposalDTO> proposalDTOs = proposals.stream()
                    .map(ProposalDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(proposalDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Create new proposal (Principal Investigator, Project Manager can create)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL_INVESTIGATOR') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ProposalDTO> createProposal(@RequestBody Proposal proposal, HttpServletRequest request) {
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("No valid Authorization header found");
            }
            
            String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserId(jwtToken);
            
            // Find the user by userId
            User authenticatedUser = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found with ID: " + userId));
            
            // Set the required user relationships BEFORE validation
            proposal.setPrincipalInvestigator(authenticatedUser);
            proposal.setCreatedBy(authenticatedUser);
            
            // Now validate the complete proposal object
            Set<ConstraintViolation<Proposal>> violations = validator.validate(proposal);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ConstraintViolation<Proposal> violation : violations) {
                    sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
                }
                throw new RuntimeException("Validation failed: " + sb.toString());
            }
            
            Proposal createdProposal = proposalService.createProposal(proposal);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ProposalDTO(createdProposal));
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error creating proposal: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Update proposal (Admin, Principal Investigator, Project Manager can update)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL_INVESTIGATOR') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<Proposal> updateProposal(@PathVariable Long id, @RequestBody Proposal proposalDetails, HttpServletRequest request) {
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("No valid Authorization header found");
            }
            
            String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserId(jwtToken);
            
            // Find the user by userId
            User authenticatedUser = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found with ID: " + userId));
            
            // Set the required user relationships BEFORE validation
            proposalDetails.setPrincipalInvestigator(authenticatedUser);
            proposalDetails.setCreatedBy(authenticatedUser);
            
            // Now validate the complete proposal object
            Set<ConstraintViolation<Proposal>> violations = validator.validate(proposalDetails);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ConstraintViolation<Proposal> violation : violations) {
                    sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
                }
                throw new RuntimeException("Validation failed: " + sb.toString());
            }
            
            Proposal updatedProposal = proposalService.updateProposal(id, proposalDetails);
            return ResponseEntity.ok(updatedProposal);
        } catch (RuntimeException e) {
            // Log the error for debugging
            System.err.println("Error updating proposal: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    // Delete proposal (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProposal(@PathVariable Long id) {
        try {
            proposalService.deleteProposal(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Soft delete/withdraw proposal (Principal Investigator, Project Manager, Committee Chair can withdraw)
    @PutMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL_INVESTIGATOR') or hasRole('PROJECT_MANAGER') or hasRole('COMMITTEE_CHAIR')")
    public ResponseEntity<?> softDeleteProposal(@PathVariable Long id) {
        try {
            Proposal withdrawnProposal = proposalService.softDeleteProposal(id);
            return ResponseEntity.ok(withdrawnProposal);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDeleteProposal(@PathVariable Long id) {
        try {
            boolean canDelete = proposalService.canDeleteProposal(id);
            return ResponseEntity.ok(canDelete);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/principal-investigator/{piId}")
    public ResponseEntity<List<Proposal>> getProposalsByPrincipalInvestigatorId(@PathVariable Long piId) {
        User pi = new User();
        pi.setId(piId);
        List<Proposal> proposals = proposalService.getProposalsByPrincipalInvestigator(pi);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Proposal>> getProposalsByStatus(@PathVariable Proposal.Status status) {
        List<Proposal> proposals = proposalService.getProposalsByStatus(status);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/project-type/{projectType}")
    public ResponseEntity<List<Proposal>> getProposalsByProjectType(@PathVariable Proposal.ProjectType projectType) {
        List<Proposal> proposals = proposalService.getProposalsByProjectType(projectType);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Proposal>> getProposalsByDepartment(@PathVariable Long departmentId) {
        List<Proposal> proposals = proposalService.getProposalsByDepartment(departmentId);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<Proposal>> getProposalsByCreatedBy(@PathVariable Long userId) {
        User createdBy = new User();
        createdBy.setId(userId);
        List<Proposal> proposals = proposalService.getProposalsByCreatedBy(createdBy);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/deadline")
    public ResponseEntity<List<Proposal>> getProposalsByDeadlineRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Proposal> proposals = proposalService.getProposalsBySubmissionDeadline(startDate, endDate);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<Proposal>> getProposalsByStatusAndDepartment(
            @RequestParam Proposal.Status status,
            @RequestParam Long departmentId) {
        List<Proposal> proposals = proposalService.getProposalsByStatusAndDepartment(status, departmentId);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/pi-status")
    public ResponseEntity<List<Proposal>> getProposalsByPIAndStatus(
            @RequestParam Long piId,
            @RequestParam Proposal.Status status) {
        List<Proposal> proposals = proposalService.getProposalsByPIAndStatus(piId, status);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Proposal>> searchProposals(@RequestParam String keyword) {
        List<Proposal> proposals = proposalService.searchProposals(keyword);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/budget-range")
    public ResponseEntity<List<Proposal>> getProposalsByBudgetRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        List<Proposal> proposals = proposalService.getProposalsByBudgetRange(minAmount, maxAmount);
        return ResponseEntity.ok(proposals);
    }
    
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getProposalCountByStatus(@PathVariable Proposal.Status status) {
        Long count = proposalService.getProposalCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/upcoming-deadlines")
    public ResponseEntity<List<Proposal>> getUpcomingDeadlines(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam List<Proposal.Status> statuses) {
        List<Proposal> proposals = proposalService.getUpcomingDeadlines(date, statuses);
        return ResponseEntity.ok(proposals);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Proposal> updateProposalStatus(@PathVariable Long id, @RequestBody Proposal.Status status) {
        try {
            Proposal updatedProposal = proposalService.updateProposalStatus(id, status);
            return ResponseEntity.ok(updatedProposal);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/submit")
    public ResponseEntity<Proposal> submitProposal(@PathVariable Long id) {
        try {
            Proposal submittedProposal = proposalService.submitProposal(id);
            return ResponseEntity.ok(submittedProposal);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Proposal> withdrawProposal(@PathVariable Long id) {
        try {
            Proposal withdrawnProposal = proposalService.withdrawProposal(id);
            return ResponseEntity.ok(withdrawnProposal);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Debug endpoint to check JWT authentication details
    @GetMapping("/debug/user-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();
        
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("username", username);
        userInfo.put("authorities", authorities);
        userInfo.put("authType", authentication.getClass().getSimpleName());
        
        // Try to find user in database
        try {
            User user = userService.getUserByUsername(username).orElse(null);
            userInfo.put("userFound", user != null);
            if (user != null) {
                userInfo.put("userId", user.getId());
                userInfo.put("role", user.getRole());
                userInfo.put("email", user.getEmail());
            }
        } catch (Exception e) {
            userInfo.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(userInfo);
    }
    
    // Upload document for a proposal
    @PostMapping("/{proposalId}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long proposalId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        
        try {
            // Validate proposal exists
            Optional<Proposal> proposalOpt = proposalService.getProposalById(proposalId);
            if (!proposalOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Proposal not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Proposal proposal = proposalOpt.get();
            
            // Get current user
            String authHeader = request.getHeader("Authorization");
            User currentUser = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                Long userId = jwtUtil.extractUserId(jwtToken);
                currentUser = userService.getUserById(userId).orElse(null);
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File is empty");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validate file type (PDF, DOC, DOCX, TXT)
            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            
            if (contentType == null || originalFilename == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Invalid file");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean validFileType = contentType.equals("application/pdf") ||
                                   contentType.equals("application/msword") ||
                                   contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                                   contentType.equals("text/plain");
            
            if (!validFileType) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Invalid file type. Only PDF, DOC, DOCX, and TXT files are allowed");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create upload directory if it doesn't exist
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }
            
            // Generate unique filename
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                fileExtension = originalFilename.substring(dotIndex);
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file to disk
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create Document entity
            Document document = new Document();
            document.setFileName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileSize(file.getSize());
            document.setFileType(contentType);
            document.setDocumentType(Document.DocumentType.PROPOSAL_DOCUMENT);
            document.setDescription(description);
            document.setProposal(proposal);
            document.setUploadedBy(currentUser);
            document.setUploadedDate(java.time.LocalDateTime.now());
            document.setIsActive(true);
            
            // Save document
            Document savedDocument = documentService.createDocument(document);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document uploaded successfully");
            response.put("documentId", savedDocument.getId());
            response.put("fileName", savedDocument.getFileName());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error uploading document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get all documents for a proposal
    @GetMapping("/{proposalId}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Document>> getProposalDocuments(@PathVariable Long proposalId) {
        try {
            List<Document> documents = documentService.getDocumentsByProposalId(proposalId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
