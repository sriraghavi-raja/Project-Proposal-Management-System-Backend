package com.proposalmanagement.service;

import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.entity.WorkflowState;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.repository.WorkflowStateRepository;
import com.proposalmanagement.repository.ProposalRepository;
import com.proposalmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WorkflowStateService {
    
    @Autowired
    private WorkflowStateRepository workflowStateRepository;
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public WorkflowState createWorkflowState(WorkflowState workflowState) {
        validateWorkflowState(workflowState);
        
        // Ensure proposal exists
        if (workflowState.getProposal() == null || workflowState.getProposal().getId() == null) {
            throw new InvalidOperationException("Workflow state must be associated with a valid proposal");
        }
        
        Proposal proposal = proposalRepository.findById(workflowState.getProposal().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + workflowState.getProposal().getId()));
        
        workflowState.setProposal(proposal);
        workflowState.setIsActive(true);
        workflowState.setEntryDate(LocalDateTime.now());
        
        // Validate assigned user if provided
        if (workflowState.getAssignedTo() != null && workflowState.getAssignedTo().getId() != null) {
            User assignedUser = userRepository.findById(workflowState.getAssignedTo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + workflowState.getAssignedTo().getId()));
            workflowState.setAssignedTo(assignedUser);
        }
        
        // Set created by user if provided
        if (workflowState.getCreatedBy() != null && workflowState.getCreatedBy().getId() != null) {
            User createdByUser = userRepository.findById(workflowState.getCreatedBy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + workflowState.getCreatedBy().getId()));
            workflowState.setCreatedBy(createdByUser);
        }
        
        WorkflowState savedWorkflowState = workflowStateRepository.save(workflowState);
        
        // Send notification if assigned to a user
        if (savedWorkflowState.getAssignedTo() != null) {
            notificationService.sendWorkflowAssignmentNotification(savedWorkflowState);
        }
        
        return savedWorkflowState;
    }
    
    public WorkflowState getWorkflowStateById(Long id) {
        return workflowStateRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow state not found with id: " + id));
    }
    
    public List<WorkflowState> getWorkflowStatesByProposal(Long proposalId) {
        return workflowStateRepository.findByProposalIdOrderByEntryDateDesc(proposalId);
    }
    
    public List<WorkflowState> getActiveWorkflowStatesByProposal(Long proposalId) {
        return workflowStateRepository.findActiveByProposalId(proposalId);
    }
    
    public Optional<WorkflowState> getCurrentWorkflowState(Long proposalId) {
        return workflowStateRepository.findCurrentStateByProposal(proposalId);
    }
    
    public List<WorkflowState> getWorkflowStatesByStageAndStatus(String stage, WorkflowState.Status status) {
        return workflowStateRepository.findByStageAndStatusOrderByEntryDateDesc(stage, status);
    }
    
    public List<WorkflowState> getPendingTasksByUser(Long userId) {
        return workflowStateRepository.findPendingTasksByUser(userId);
    }
    
    public List<WorkflowState> getTasksByUserAndStatus(Long userId, WorkflowState.Status status) {
        return workflowStateRepository.findByAssignedToAndStatus(userId, status);
    }
    
    public List<WorkflowState> getOverdueTasks() {
        return workflowStateRepository.findOverdueTasks(LocalDateTime.now());
    }
    
    public List<WorkflowState> getTasksDueInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return workflowStateRepository.findTasksDueInDateRange(startDate, endDate);
    }
    
    public WorkflowState updateWorkflowState(Long id, WorkflowState workflowState) {
        WorkflowState existingWorkflowState = getWorkflowStateById(id);
        
        // Update fields
        existingWorkflowState.setStage(workflowState.getStage());
        existingWorkflowState.setStatus(workflowState.getStatus());
        existingWorkflowState.setComments(workflowState.getComments());
        existingWorkflowState.setDueDate(workflowState.getDueDate());
        
        // Update assigned user if provided
        if (workflowState.getAssignedTo() != null && workflowState.getAssignedTo().getId() != null) {
            User assignedUser = userRepository.findById(workflowState.getAssignedTo().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + workflowState.getAssignedTo().getId()));
            
            // Check if assignment changed
            boolean assignmentChanged = existingWorkflowState.getAssignedTo() == null || 
                !existingWorkflowState.getAssignedTo().getId().equals(assignedUser.getId());
            
            existingWorkflowState.setAssignedTo(assignedUser);
            
            if (assignmentChanged) {
                notificationService.sendWorkflowAssignmentNotification(existingWorkflowState);
            }
        }
        
        validateWorkflowState(existingWorkflowState);
        return workflowStateRepository.save(existingWorkflowState);
    }
    
    public WorkflowState completeWorkflowState(Long id, String completionComments, User completedByUser) {
        WorkflowState workflowState = getWorkflowStateById(id);
        
        if (workflowState.getStatus() == WorkflowState.Status.COMPLETED) {
            throw new InvalidOperationException("Workflow state is already completed");
        }
        
        workflowState.setStatus(WorkflowState.Status.COMPLETED);
        workflowState.setCompletedDate(LocalDateTime.now());
        workflowState.setCompletedBy(completedByUser);
        
        if (completionComments != null && !completionComments.trim().isEmpty()) {
            String existingComments = workflowState.getComments() != null ? workflowState.getComments() : "";
            workflowState.setComments(existingComments + "\n[Completed] " + completionComments);
        }
        
        WorkflowState completedWorkflow = workflowStateRepository.save(workflowState);
        
        // Send completion notification
        notificationService.sendWorkflowCompletionNotification(completedWorkflow);
        
        return completedWorkflow;
    }
    
    public WorkflowState assignWorkflowState(Long id, Long userId, LocalDateTime dueDate) {
        WorkflowState workflowState = getWorkflowStateById(id);
        
        User assignedUser = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        workflowState.setAssignedTo(assignedUser);
        workflowState.setDueDate(dueDate);
        workflowState.setStatus(WorkflowState.Status.PENDING);
        
        WorkflowState updatedWorkflow = workflowStateRepository.save(workflowState);
        
        // Send assignment notification
        notificationService.sendWorkflowAssignmentNotification(updatedWorkflow);
        
        return updatedWorkflow;
    }
    
    public WorkflowState advanceProposalToNextStage(Long proposalId, String nextStage, User actionUser) {
        // Complete current workflow state
        Optional<WorkflowState> currentStateOpt = getCurrentWorkflowState(proposalId);
        if (currentStateOpt.isPresent()) {
            WorkflowState currentState = currentStateOpt.get();
            if (currentState.getStatus() != WorkflowState.Status.COMPLETED) {
                completeWorkflowState(currentState.getId(), "Advanced to next stage", actionUser);
            }
        }
        
        // Create new workflow state for next stage
        Proposal proposal = proposalRepository.findById(proposalId)
            .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));
        
        WorkflowState newWorkflowState = new WorkflowState();
        newWorkflowState.setProposal(proposal);
        newWorkflowState.setStage(nextStage);
        newWorkflowState.setStatus(WorkflowState.Status.PENDING);
        newWorkflowState.setCreatedBy(actionUser);
        newWorkflowState.setEntryDate(LocalDateTime.now());
        
        return workflowStateRepository.save(newWorkflowState);
    }
    
    public void deleteWorkflowState(Long id) {
        WorkflowState workflowState = getWorkflowStateById(id);
        workflowState.setIsActive(false);
        workflowStateRepository.save(workflowState);
    }
    
    public List<Object[]> getWorkflowStageDistribution() {
        return workflowStateRepository.getWorkflowStageDistribution();
    }
    
    public Double getAverageCompletionTimeByStage(String stage) {
        return workflowStateRepository.getAverageCompletionTimeHoursByStage(stage);
    }
    
    public Long getTaskCountByUserAndStatus(Long userId, WorkflowState.Status status) {
        return workflowStateRepository.countByAssignedToAndStatus(userId, status);
    }
    
    public List<WorkflowState> getWorkflowStatesByProposalAndStage(Long proposalId, String stage) {
        return workflowStateRepository.findByProposalIdAndStage(proposalId, stage);
    }
    
    private void validateWorkflowState(WorkflowState workflowState) {
        if (workflowState.getStage() == null || workflowState.getStage().trim().isEmpty()) {
            throw new InvalidOperationException("Workflow stage is required");
        }
        
        if (workflowState.getStatus() == null) {
            throw new InvalidOperationException("Workflow status is required");
        }
        
        if (workflowState.getDueDate() != null && workflowState.getDueDate().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Due date cannot be in the past");
        }
    }
}