package com.proposalmanagement.security;

import com.proposalmanagement.entity.Milestone;
import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.MilestoneRepository;
import com.proposalmanagement.repository.ProjectRepository;
import com.proposalmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("milestoneSecurity")
public class MilestoneSecurity {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getPrincipal().toString();
        }
        
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean isPiOfProject(Long projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        Optional<Project> project = projectRepository.findById(projectId);
        
        return project.isPresent() && 
               project.get().getProposal() != null &&
               project.get().getProposal().getPrincipalInvestigator() != null &&
               project.get().getProposal().getPrincipalInvestigator().getId().equals(currentUser.getId());
    }

    public boolean isPiOfMilestone(Long milestoneId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        Optional<Milestone> milestone = milestoneRepository.findById(milestoneId);
        
        return milestone.isPresent() && 
               milestone.get().getProject() != null &&
               milestone.get().getProject().getProposal() != null &&
               milestone.get().getProject().getProposal().getPrincipalInvestigator() != null &&
               milestone.get().getProject().getProposal().getPrincipalInvestigator().getId().equals(currentUser.getId());
    }

    public boolean isReviewerOfProject(Long projectId) {
        return true; // Allow reviewers to view milestones
    }

    public boolean isReviewerOfMilestone(Long milestoneId) {
        return true; // Allow reviewers to view milestones
    }
}