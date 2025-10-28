package com.proposalmanagement.controller;

import com.proposalmanagement.entity.User;
import com.proposalmanagement.service.UserService;
import com.proposalmanagement.dto.UpdateProfileRequest;
import com.proposalmanagement.entity.Department;
import com.proposalmanagement.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    // Get all users (Permissions now handled in SecurityConfig)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    // Create user (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update user 
    // - Admins and Department Heads can update any user
    // - Other users can only update their own profile
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody User userDetails,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token and get current user
            String token = authHeader.replace("Bearer ", "");
            User currentUser = userService.getUserFromToken(token);
            
            // Check authorization
            boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;
            boolean isDeptHead = currentUser.getRole() == User.Role.DEPARTMENT_HEAD;
            boolean isUpdatingSelf = currentUser.getId().equals(id);
            
            if (!isAdmin && !isDeptHead && !isUpdatingSelf) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // If user is updating themselves (and not admin/dept head), restrict certain fields
            if (isUpdatingSelf && !isAdmin && !isDeptHead) {
                // Preserve protected fields for non-admin self-updates
                User existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                userDetails.setUsername(existingUser.getUsername());
                userDetails.setRole(existingUser.getRole());
                userDetails.setPasswordHash(existingUser.getPasswordHash());
                userDetails.setEmailVerified(existingUser.getEmailVerified());
                userDetails.setTwoFactorEnabled(existingUser.getTwoFactorEnabled());
            }
            
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Delete user (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable User.Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}/active")
    public ResponseEntity<List<User>> getActiveUsersByRole(@PathVariable User.Role role) {
        List<User> users = userService.getActiveUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<User>> getUsersByDepartment(@PathVariable String department) {
        List<User> users = userService.getUsersByDepartment(department);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/department/{department}/active")
    public ResponseEntity<List<User>> getActiveUsersByDepartment(@PathVariable String department) {
        List<User> users = userService.getActiveUsersByDepartment(department);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchActiveUsers(@RequestParam String keyword) {
        List<User> users = userService.searchActiveUsers(keyword);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{id}/last-login")
    public ResponseEntity<User> updateLastLogin(@PathVariable Long id) {
        try {
            User updatedUser = userService.updateLastLogin(id);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/password")
    public ResponseEntity<User> updatePassword(@PathVariable Long id, @RequestBody String newPasswordHash) {
        try {
            User updatedUser = userService.updatePassword(id, newPasswordHash);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
    
    // Get current user profile
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from Authorization header
            String token = authHeader.replace("Bearer ", "");
            User user = userService.getUserFromToken(token);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or expired token: " + e.getMessage());
        }
    }
    
    // Update current user profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest profileRequest) {
        try {
            // Extract token from Authorization header
            String token = authHeader.replace("Bearer ", "");
            User currentUser = userService.getUserFromToken(token);
            
            // Validate if the user has permission to update these fields
            if (!profileRequest.validateUpdates(currentUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You don't have permission to update these fields");
            }
            
            // Update basic info (available to all users)
            if (profileRequest.getFirstName() != null) {
                currentUser.setFirstName(profileRequest.getFirstName());
            }
            if (profileRequest.getLastName() != null) {
                currentUser.setLastName(profileRequest.getLastName());
            }
            if (profileRequest.getEmail() != null) {
                // Check if email is already taken by another user
                if (!currentUser.getEmail().equals(profileRequest.getEmail()) && 
                    userService.existsByEmail(profileRequest.getEmail())) {
                    return ResponseEntity.badRequest()
                        .body("Email already exists: " + profileRequest.getEmail());
                }
                currentUser.setEmail(profileRequest.getEmail());
            }
            if (profileRequest.getPhoneNumber() != null) {
                currentUser.setPhoneNumber(profileRequest.getPhoneNumber());
            }
            
            // Update professional info (if user has permission)
            if (canUpdateProfessionalInfo(currentUser.getRole())) {
                if (profileRequest.getOrganizationId() != null) {
                    currentUser.setOrganizationId(profileRequest.getOrganizationId());
                }
                if (profileRequest.getOfficeLocation() != null) {
                    currentUser.setOfficeLocation(profileRequest.getOfficeLocation());
                }
                if (profileRequest.getExpertiseAreas() != null) {
                    currentUser.setExpertiseAreas(profileRequest.getExpertiseAreas());
                }
            }
            
            // Update restricted info (if user has permission)
            if (canUpdateRestrictedInfo(currentUser.getRole())) {
                if (profileRequest.getDepartment() != null && profileRequest.getDepartment().getId() != null) {
                    Department dept = departmentRepository.findById(profileRequest.getDepartment().getId())
                        .orElseThrow(() -> new RuntimeException("Department not found"));
                    currentUser.setDepartment(dept);
                } else if (profileRequest.getDepartment() == null) {
                    currentUser.setDepartment(null);
                }
            }
            
            User updatedUser = userService.updateUser(currentUser.getId(), currentUser);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Check if the user role can update professional information
     */
    private boolean canUpdateProfessionalInfo(User.Role role) {
        return role == User.Role.ADMIN ||
               role == User.Role.DEPARTMENT_HEAD ||
               role == User.Role.PROJECT_MANAGER ||
               role == User.Role.PRINCIPAL_INVESTIGATOR ||
               role == User.Role.COMMITTEE_CHAIR ||
               role == User.Role.FINANCIAL_OFFICER;
    }
    
    /**
     * Check if the user role can update restricted information
     */
    private boolean canUpdateRestrictedInfo(User.Role role) {
        return role == User.Role.ADMIN ||
               role == User.Role.DEPARTMENT_HEAD;
    }
}