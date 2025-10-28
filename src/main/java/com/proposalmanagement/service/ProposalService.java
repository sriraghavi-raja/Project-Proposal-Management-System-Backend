package com.proposalmanagement.service;

import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.*;
import com.proposalmanagement.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProposalService {
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private BudgetItemRepository budgetItemRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    // @Autowired
    // private WorkflowRepository workflowRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }
    
    public Optional<Proposal> getProposalById(Long id) {
        return proposalRepository.findById(id);
    }
    
    public Proposal createProposal(Proposal proposal) {
        return proposalRepository.save(proposal);
    }
    
    public Proposal updateProposal(Long id, Proposal proposalDetails) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        proposal.setTitle(proposalDetails.getTitle());
        proposal.setAbstract_(proposalDetails.getAbstract_());
        proposal.setPrincipalInvestigator(proposalDetails.getPrincipalInvestigator());
        proposal.setCoInvestigators(proposalDetails.getCoInvestigators());
        proposal.setProjectType(proposalDetails.getProjectType());
        proposal.setFundingAgency(proposalDetails.getFundingAgency());
        proposal.setRequestedAmount(proposalDetails.getRequestedAmount());
        proposal.setProjectDurationMonths(proposalDetails.getProjectDurationMonths());
        proposal.setSubmissionDeadline(proposalDetails.getSubmissionDeadline());
        proposal.setStatus(proposalDetails.getStatus());
        proposal.setProjectStage(proposalDetails.getProjectStage());
        proposal.setPriorityLevel(proposalDetails.getPriorityLevel());
        proposal.setDepartmentId(proposalDetails.getDepartmentId());
        
        return proposalRepository.save(proposal);
    }
    
    public void deleteProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        // Business rule: Cannot delete approved proposals that have an associated project
        if (proposal.getStatus() == Proposal.Status.APPROVED) {
            Optional<com.proposalmanagement.entity.Project> associatedProject = projectRepository.findByProposalId(id);
            if (associatedProject.isPresent()) {
                throw new InvalidOperationException("Cannot delete approved proposal with an active project. " +
                    "Please complete or cancel the project first.");
            }
        }
        
        // Business rule: Restrict deletion of submitted proposals under review
        if (proposal.getStatus() == Proposal.Status.UNDER_REVIEW) {
            throw new InvalidOperationException("Cannot delete proposal that is currently under review. " +
                "Please withdraw the proposal first.");
        }
        
        try {
            // Delete related entities in proper order to avoid foreign key constraints
            
            // 1. Delete notifications related to this proposal
            notificationRepository.deleteByRelatedProposalId(id);
            
            // 2. Delete workflows for this proposal
            // workflowRepository.deleteByProposalId(id);
            
            // 3. Delete documents associated with this proposal
            documentRepository.deleteByProposalId(id);
            
            // 4. Delete evaluations for this proposal
            evaluationRepository.deleteByProposalId(id);
            
            // 5. Delete budget items for this proposal
            budgetItemRepository.deleteByProposalId(id);
            
            // 6. Finally, delete the proposal
            proposalRepository.delete(proposal);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete proposal and its related data: " + e.getMessage(), e);
        }
    }
    
    public List<Proposal> getProposalsByPrincipalInvestigator(User principalInvestigator) {
        return proposalRepository.findByPrincipalInvestigator(principalInvestigator);
    }
    
    public List<Proposal> getProposalsByStatus(Proposal.Status status) {
        return proposalRepository.findByStatus(status);
    }
    
    public List<Proposal> getProposalsByProjectType(Proposal.ProjectType projectType) {
        return proposalRepository.findByProjectType(projectType);
    }
    
    public List<Proposal> getProposalsByDepartment(Long departmentId) {
        return proposalRepository.findByDepartmentId(departmentId);
    }
    
    public List<Proposal> getProposalsByCreatedBy(User createdBy) {
        return proposalRepository.findByCreatedBy(createdBy);
    }
    
    public List<Proposal> getProposalsBySubmissionDeadline(LocalDate startDate, LocalDate endDate) {
        return proposalRepository.findBySubmissionDeadlineBetween(startDate, endDate);
    }
    
    public List<Proposal> getProposalsByStatusAndDepartment(Proposal.Status status, Long departmentId) {
        return proposalRepository.findByStatusAndDepartmentId(status, departmentId);
    }
    
    public List<Proposal> getProposalsByPIAndStatus(Long piId, Proposal.Status status) {
        return proposalRepository.findByPrincipalInvestigatorIdAndStatus(piId, status);
    }
    
    public List<Proposal> searchProposals(String keyword) {
        return proposalRepository.searchByTitleOrAbstract(keyword);
    }
    
    public List<Proposal> getProposalsByBudgetRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return proposalRepository.findByRequestedAmountBetween(minAmount, maxAmount);
    }
    
    public Long getProposalCountByStatus(Proposal.Status status) {
        return proposalRepository.countByStatus(status);
    }
    
    public List<Proposal> getUpcomingDeadlines(LocalDate date, List<Proposal.Status> statuses) {
        return proposalRepository.findUpcomingDeadlines(date, statuses);
    }
    
    public Proposal updateProposalStatus(Long id, Proposal.Status status) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        proposal.setStatus(status);
        return proposalRepository.save(proposal);
    }
    
    public Proposal submitProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        if (proposal.getStatus() != Proposal.Status.DRAFT) {
            throw new RuntimeException("Only draft proposals can be submitted");
        }
        
        proposal.setStatus(Proposal.Status.SUBMITTED);
        return proposalRepository.save(proposal);
    }
    
    public Proposal withdrawProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        if (proposal.getStatus() == Proposal.Status.APPROVED || 
            proposal.getStatus() == Proposal.Status.REJECTED) {
            throw new RuntimeException("Cannot withdraw approved or rejected proposals");
        }
        
        proposal.setStatus(Proposal.Status.WITHDRAWN);
        return proposalRepository.save(proposal);
    }
    
    /**
     * Soft delete a proposal by changing its status to WITHDRAWN
     * This is safer than hard deletion and preserves data integrity
     */
    public Proposal softDeleteProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        // Only allow soft deletion of non-approved proposals
        if (proposal.getStatus() == Proposal.Status.APPROVED) {
            throw new InvalidOperationException("Cannot delete approved proposal. " +
                "Please contact an administrator for assistance.");
        }
        
        proposal.setStatus(Proposal.Status.WITHDRAWN);
        return proposalRepository.save(proposal);
    }
    
    /**
     * Check if a proposal can be safely deleted
     */
    public boolean canDeleteProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
        
        // Cannot delete approved proposals with active projects
        if (proposal.getStatus() == Proposal.Status.APPROVED) {
            Optional<com.proposalmanagement.entity.Project> project = projectRepository.findByProposalId(id);
            return !project.isPresent();
        }
        
        // Cannot delete proposals under review
        return proposal.getStatus() != Proposal.Status.UNDER_REVIEW;
    }
}