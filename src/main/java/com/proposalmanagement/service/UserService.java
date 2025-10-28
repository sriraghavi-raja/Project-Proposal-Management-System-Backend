package com.proposalmanagement.service;

import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.*;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // @Autowired
    // private WorkflowRepository workflowRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Check username uniqueness if changed
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDetails.getUsername());
        }
        
        // Check email uniqueness if changed
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
        }
        
        // Update fields from userDetails, handling empty strings
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getFirstName() != null && !userDetails.getFirstName().trim().isEmpty()) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null && !userDetails.getLastName().trim().isEmpty()) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getDepartment() != null) {
            user.setDepartment(userDetails.getDepartment());
        }
        if (userDetails.getOrganizationId() != null) {
            user.setOrganizationId(userDetails.getOrganizationId().trim().isEmpty() ? null : userDetails.getOrganizationId());
        }
        if (userDetails.getPhoneNumber() != null) {
            user.setPhoneNumber(userDetails.getPhoneNumber().trim().isEmpty() ? null : userDetails.getPhoneNumber());
        }
        if (userDetails.getOfficeLocation() != null) {
            user.setOfficeLocation(userDetails.getOfficeLocation().trim().isEmpty() ? null : userDetails.getOfficeLocation());
        }
        if (userDetails.getExpertiseAreas() != null) {
            user.setExpertiseAreas(userDetails.getExpertiseAreas().trim().isEmpty() ? null : userDetails.getExpertiseAreas());
        }
        if (userDetails.getIsActive() != null) {
            user.setIsActive(userDetails.getIsActive());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Business rule: Cannot delete users who are principal investigators of active proposals
        List<com.proposalmanagement.entity.Proposal> activeProposals = proposalRepository.findByPrincipalInvestigatorIdAndStatus(id, 
            com.proposalmanagement.entity.Proposal.Status.SUBMITTED);
        if (!activeProposals.isEmpty()) {
            throw new InvalidOperationException("Cannot delete user who is principal investigator of active proposals. " +
                "Please reassign proposals first.");
        }
        
        // Check for proposals under review
        List<com.proposalmanagement.entity.Proposal> reviewProposals = proposalRepository.findByPrincipalInvestigatorIdAndStatus(id, 
            com.proposalmanagement.entity.Proposal.Status.UNDER_REVIEW);
        if (!reviewProposals.isEmpty()) {
            throw new InvalidOperationException("Cannot delete user who has proposals under review. " +
                "Please wait for review completion or reassign proposals.");
        }
        
        try {
            // Delete related entities in proper order
            
            // 1. Delete notifications for this user
            notificationRepository.deleteByUserId(id);
            
            // 2. Delete workflows assigned to this user
            // workflowRepository.deleteByAssignedToId(id);
            
            // 3. Delete evaluations by this user
            evaluationRepository.deleteByReviewerId(id);
            
            // 4. Finally, delete the user
            userRepository.delete(user);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user and related data: " + e.getMessage(), e);
        }
    }
    
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(true);
        userRepository.save(user);
    }
    
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> getActiveUsersByRole(User.Role role) {
        return userRepository.findActiveUsersByRole(role);
    }
    
    public List<User> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department);
    }
    
    public List<User> getActiveUsersByDepartment(String department) {
        return userRepository.findActiveUsersByDepartment(department);
    }
    
    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    public List<User> searchActiveUsers(String keyword) {
        return userRepository.searchActiveUsers(keyword);
    }
    
    public User updateLastLogin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User updatePassword(Long id, String newPasswordHash) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setPasswordHash(newPasswordHash);
        return userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User getUserFromToken(String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}