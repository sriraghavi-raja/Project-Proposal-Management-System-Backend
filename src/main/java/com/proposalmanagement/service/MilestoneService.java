package com.proposalmanagement.service;

import com.proposalmanagement.entity.Milestone;
import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.repository.MilestoneRepository;
import com.proposalmanagement.repository.ProjectRepository;
import com.proposalmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MilestoneService {
    
    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public Milestone createMilestone(Milestone milestone) {
        validateMilestone(milestone);
        
        // Ensure project exists
        if (milestone.getProject() == null || milestone.getProject().getId() == null) {
            throw new InvalidOperationException("Milestone must be associated with a valid project");
        }
        
        Project project = projectRepository.findById(milestone.getProject().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + milestone.getProject().getId()));
        
        milestone.setProject(project);
        milestone.setIsActive(true);
        
        // Validate assigned user if provided
        if (milestone.getAssignedTo() != null && milestone.getAssignedTo().getId() != null) {
            User assignedUser = userRepository.findById(milestone.getAssignedTo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + milestone.getAssignedTo().getId()));
            milestone.setAssignedTo(assignedUser);
        }
        
        Milestone savedMilestone = milestoneRepository.save(milestone);
        
        // Send notification if assigned to a user
        if (savedMilestone.getAssignedTo() != null) {
            notificationService.sendMilestoneAssignmentNotification(savedMilestone);
        }
        
        return savedMilestone;
    }
    
    public Milestone getMilestoneById(Long id) {
        return milestoneRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id: " + id));
    }
    
    public List<Milestone> getMilestonesByProjectId(Long projectId) {
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId);
    }
    
    public List<Milestone> getActiveMilestonesByProject(Long projectId) {
        return milestoneRepository.findActiveByProjectId(projectId);
    }
    
    public List<Milestone> getMilestonesByStatus(Long projectId, Milestone.Status status) {
        return milestoneRepository.findByProjectIdAndStatusOrderByDueDateAsc(projectId, status);
    }
    
    public List<Milestone> getMilestonesByAssignedUser(Long userId, Milestone.Status status) {
        return milestoneRepository.findByAssignedToAndStatus(userId, status);
    }
    
    public List<Milestone> getOverdueMilestones() {
        return milestoneRepository.findOverdueMilestones(LocalDate.now(), Milestone.Status.COMPLETED);
    }
    
    public List<Milestone> getMilestonesDueInDateRange(LocalDate startDate, LocalDate endDate) {
        return milestoneRepository.findMilestonesByDateRange(startDate, endDate);
    }
    
    public Milestone updateMilestone(Long id, Milestone milestone) {
        Milestone existingMilestone = getMilestoneById(id);
        
        // Update fields
        existingMilestone.setTitle(milestone.getTitle());
        existingMilestone.setDescription(milestone.getDescription());
        existingMilestone.setDueDate(milestone.getDueDate());
        existingMilestone.setCompletionPercentage(milestone.getCompletionPercentage());
        existingMilestone.setStatus(milestone.getStatus());
        existingMilestone.setPriority(milestone.getPriority());
        
        // Update assigned user if provided
        if (milestone.getAssignedTo() != null && milestone.getAssignedTo().getId() != null) {
            User assignedUser = userRepository.findById(milestone.getAssignedTo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + milestone.getAssignedTo().getId()));
            
            // Check if assignment changed
            boolean assignmentChanged = existingMilestone.getAssignedTo() == null || 
                !existingMilestone.getAssignedTo().getId().equals(assignedUser.getId());
            
            existingMilestone.setAssignedTo(assignedUser);
            
            if (assignmentChanged) {
                notificationService.sendMilestoneAssignmentNotification(existingMilestone);
            }
        }
        
        validateMilestone(existingMilestone);
        
        Milestone updatedMilestone = milestoneRepository.save(existingMilestone);
        
        // Send completion notification if milestone is completed
        if (milestone.getStatus() == Milestone.Status.COMPLETED && 
            existingMilestone.getStatus() != Milestone.Status.COMPLETED) {
            notificationService.sendMilestoneCompletionNotification(updatedMilestone);
        }
        
        return updatedMilestone;
    }
    
    public Milestone updateMilestoneProgress(Long id, BigDecimal completionPercentage) {
        Milestone milestone = getMilestoneById(id);
        
        if (completionPercentage.compareTo(BigDecimal.ZERO) < 0 || 
            completionPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidOperationException("Completion percentage must be between 0 and 100");
        }
        
        milestone.setCompletionPercentage(completionPercentage);
        
        // Auto-update status based on completion
        if (completionPercentage.compareTo(BigDecimal.ZERO) == 0) {
            milestone.setStatus(Milestone.Status.NOT_STARTED);
        } else if (completionPercentage.compareTo(new BigDecimal("100")) == 0) {
            milestone.setStatus(Milestone.Status.COMPLETED);
            milestone.setCompletedDate(LocalDate.now());
            notificationService.sendMilestoneCompletionNotification(milestone);
        } else {
            milestone.setStatus(Milestone.Status.IN_PROGRESS);
        }
        
        return milestoneRepository.save(milestone);
    }
    
    public void deleteMilestone(Long id) {
        Milestone milestone = getMilestoneById(id);
        milestone.setIsActive(false);
        milestoneRepository.save(milestone);
    }
    
    public Double getProjectCompletionPercentage(Long projectId) {
        return milestoneRepository.getAverageCompletionPercentageByProject(projectId);
    }
    
    public List<Milestone> getCompletedMilestonesByProject(Long projectId) {
        return milestoneRepository.findCompletedMilestonesByProject(projectId);
    }
    
    public Long getMilestoneCountByStatus(Long projectId, Milestone.Status status) {
        return milestoneRepository.countByProjectIdAndStatus(projectId, status);
    }
    
    public List<Milestone> getMilestonesByPrincipalInvestigator(Long userId) {
        return milestoneRepository.findByPrincipalInvestigator(userId);
    }
    
    public List<Milestone> getMilestonesDueToday() {
        LocalDate today = LocalDate.now();
        List<Milestone.Status> activeStatuses = List.of(
            Milestone.Status.NOT_STARTED, 
            Milestone.Status.IN_PROGRESS
        );
        return milestoneRepository.findMilestonesDueOn(today, activeStatuses);
    }
    
    private void validateMilestone(Milestone milestone) {
        if (milestone.getTitle() == null || milestone.getTitle().trim().isEmpty()) {
            throw new InvalidOperationException("Milestone title is required");
        }
        
        if (milestone.getDueDate() == null) {
            throw new InvalidOperationException("Milestone due date is required");
        }
        
        if (milestone.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Milestone due date cannot be in the past");
        }
        
        if (milestone.getCompletionPercentage() != null) {
            if (milestone.getCompletionPercentage().compareTo(BigDecimal.ZERO) < 0 || 
                milestone.getCompletionPercentage().compareTo(new BigDecimal("100")) > 0) {
                throw new InvalidOperationException("Completion percentage must be between 0 and 100");
            }
        }
    }
}