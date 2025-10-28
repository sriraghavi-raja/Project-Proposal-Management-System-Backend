package com.proposalmanagement.service;

import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.repository.ProjectRepository;
import com.proposalmanagement.repository.DocumentRepository;
import com.proposalmanagement.repository.NotificationRepository;
import com.proposalmanagement.repository.ProposalRepository;
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
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }
    
    public Optional<Project> getProjectByProposal(Proposal proposal) {
        return projectRepository.findByProposal(proposal);
    }
    
    public Optional<Project> getProjectByProposalId(Long proposalId) {
        return projectRepository.findByProposalId(proposalId);
    }
    
    public Optional<Project> getProjectByNumber(String projectNumber) {
        return projectRepository.findByProjectNumber(projectNumber);
    }
    
    public Project createProject(Project project) {
        // Fetch the existing proposal entity
        Long proposalId = project.getProposal().getId();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + proposalId));
        
        // Check if proposal is approved
        if (!Proposal.Status.APPROVED.equals(proposal.getStatus())) {
            throw new RuntimeException("Only approved proposals can be converted to projects. Current status: " + proposal.getStatus());
        }
        
        // Check if project already exists for this proposal
        Optional<Project> existingProject = projectRepository.findByProposalId(proposalId);
        if (existingProject.isPresent()) {
            throw new RuntimeException("Project already exists for this proposal");
        }
        
        // Check if project number already exists
        if (project.getProjectNumber() != null && 
            projectRepository.findByProjectNumber(project.getProjectNumber()).isPresent()) {
            throw new RuntimeException("Project number already exists: " + project.getProjectNumber());
        }
        
        // Set the managed proposal entity
        project.setProposal(proposal);
        
        return projectRepository.save(project);
    }
    
    public Project updateProject(Long id, Project projectDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        // Check project number uniqueness if changed
        if (projectDetails.getProjectNumber() != null && 
            !projectDetails.getProjectNumber().equals(project.getProjectNumber()) &&
            projectRepository.findByProjectNumber(projectDetails.getProjectNumber()).isPresent()) {
            throw new RuntimeException("Project number already exists: " + projectDetails.getProjectNumber());
        }
        
        project.setProjectNumber(projectDetails.getProjectNumber());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setActualStartDate(projectDetails.getActualStartDate());
        project.setActualEndDate(projectDetails.getActualEndDate());
        project.setStatus(projectDetails.getStatus());
        project.setCompletionPercentage(projectDetails.getCompletionPercentage());
        project.setBudgetUtilized(projectDetails.getBudgetUtilized());
        
        return projectRepository.save(project);
    }
    
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        // Business rule: Cannot delete active or completed projects
        if (project.getStatus() == Project.Status.ACTIVE) {
            throw new InvalidOperationException("Cannot delete active project. Please pause or complete the project first.");
        }
        
        if (project.getStatus() == Project.Status.COMPLETED && project.getBudgetUtilized().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidOperationException("Cannot delete completed project with utilized budget. Contact administrator for assistance.");
        }
        
        try {
            // Delete related entities in proper order
            
            // 1. Delete notifications related to this project
            notificationRepository.deleteByRelatedProjectId(id);
            
            // 2. Delete documents associated with this project
            documentRepository.deleteByProjectId(id);
            
            // 3. Finally, delete the project
            projectRepository.delete(project);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete project and its related data: " + e.getMessage(), e);
        }
    }
    
    public List<Project> getProjectsByStatus(Project.Status status) {
        return projectRepository.findByStatus(status);
    }
    
    public List<Project> getProjectsByStartDateRange(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findByStartDateBetween(startDate, endDate);
    }
    
    public List<Project> getProjectsByEndDateRange(LocalDate startDate, LocalDate endDate) {
        return projectRepository.findByEndDateBetween(startDate, endDate);
    }
    
    public List<Project> getActiveProjectsEndingBefore(LocalDate date) {
        return projectRepository.findActiveProjectsEndingBefore(date);
    }
    
    public List<Project> getProjectsByPrincipalInvestigator(Long piId) {
        return projectRepository.findByPrincipalInvestigatorId(piId);
    }
    
    public List<Project> getProjectsByDepartment(Long departmentId) {
        return projectRepository.findByDepartmentId(departmentId);
    }
    
    public Long getProjectCountByStatus(Project.Status status) {
        return projectRepository.countByStatus(status);
    }
    
    public List<Project> searchProjects(String keyword) {
        return projectRepository.searchByTitleOrProjectNumber(keyword);
    }
    
    public List<Project> getActiveProjectsWithLowCompletion(BigDecimal percentage) {
        return projectRepository.findActiveProjectsWithLowCompletion(percentage);
    }
    
    public Project updateProjectStatus(Long id, Project.Status status) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        project.setStatus(status);
        return projectRepository.save(project);
    }
    
    public Project updateCompletionPercentage(Long id, BigDecimal completionPercentage) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        if (completionPercentage.compareTo(BigDecimal.ZERO) < 0 || 
            completionPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Completion percentage must be between 0 and 100");
        }
        
        project.setCompletionPercentage(completionPercentage);
        
        // Auto-update status based on completion
        if (completionPercentage.compareTo(new BigDecimal("100")) == 0) {
            project.setStatus(Project.Status.COMPLETED);
            project.setActualEndDate(LocalDate.now());
        }
        
        return projectRepository.save(project);
    }
    
    public Project updateBudgetUtilized(Long id, BigDecimal budgetUtilized) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        if (budgetUtilized.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Budget utilized cannot be negative");
        }
        
        project.setBudgetUtilized(budgetUtilized);
        return projectRepository.save(project);
    }
    
    public Project startProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        if (project.getStatus() != Project.Status.PLANNING) {
            throw new RuntimeException("Only projects in planning status can be started");
        }
        
        project.setStatus(Project.Status.ACTIVE);
        project.setActualStartDate(LocalDate.now());
        return projectRepository.save(project);
    }
    
    public Project completeProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        project.setStatus(Project.Status.COMPLETED);
        project.setActualEndDate(LocalDate.now());
        project.setCompletionPercentage(new BigDecimal("100"));
        return projectRepository.save(project);
    }
    
    /**
     * Check if a project can be safely deleted
     */
    public boolean canDeleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        // Cannot delete active projects
        if (project.getStatus() == Project.Status.ACTIVE) {
            return false;
        }
        
        // Cannot delete completed projects with budget utilized
        return !(project.getStatus() == Project.Status.COMPLETED && 
                project.getBudgetUtilized().compareTo(BigDecimal.ZERO) > 0);
    }
    
    /**
     * Soft delete by changing status to CANCELLED
     */
    public Project cancelProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        
        if (project.getStatus() == Project.Status.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel completed project.");
        }
        
        project.setStatus(Project.Status.CANCELLED);
        return projectRepository.save(project);
    }
}