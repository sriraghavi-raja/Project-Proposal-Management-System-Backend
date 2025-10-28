package com.proposalmanagement.service;

import com.proposalmanagement.dto.*;
import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.exception.DuplicateResourceException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.repository.DepartmentRepository;
import com.proposalmanagement.repository.UserRepository;
import com.proposalmanagement.security.CustomUserDetailsService;
import com.proposalmanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Authenticate user using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(), 
                    loginRequest.getPassword()
                )
            );

            // Get user details from authentication
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // Check if user is active
            if (!user.isActive()) {
                throw new InvalidOperationException("Account is deactivated");
            }

            // Generate JWT tokens
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole().name());

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Calculate expiration time (24 hours from now)
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

            return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                expiresAt,
                user.isEmailVerified()
            );

        } catch (BadCredentialsException e) {
            throw new InvalidOperationException("Invalid username or password");
        } catch (Exception e) {
            throw new InvalidOperationException("Authentication failed: " + e.getMessage());
        }
    }
    
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new DuplicateResourceException("User", "username", registerRequest.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("User", "email", registerRequest.getEmail());
        }
        
        // Validate password confirmation if needed
        // This would be handled in controller layer validation
        
        // Create new user with required fields using constructor
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            passwordEncoder.encode(registerRequest.getPassword()),
            validateAndAssignRole(registerRequest.getRole())
        );
        
        // Set optional fields
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        
        // Apply RBAC: Restrict role assignment - TEMPORARILY DISABLED FOR TESTING
        User.Role requestedRole = registerRequest.getRole();
        System.out.println("DEBUG: Requested role: " + requestedRole);
        
        // TEMPORARY: Allow any role for testing
        if (requestedRole != null) {
            user.setRole(requestedRole);
            System.out.println("DEBUG: Directly assigned role: " + requestedRole);
        } else {
            user.setRole(User.Role.PRINCIPAL_INVESTIGATOR);
            System.out.println("DEBUG: No role specified, assigned default: PRINCIPAL_INVESTIGATOR");
        }
        
        // Set department if provided
        if (registerRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(registerRequest.getDepartmentId()).orElse(null);
            user.setDepartment(department);
        } else if (registerRequest.getDepartment() != null && !registerRequest.getDepartment().trim().isEmpty()) {
            Department department = departmentRepository.findByNameAndIsActiveTrue(registerRequest.getDepartment())
                .orElse(null);
            user.setDepartment(department);
        }
        
        user.setOrganizationId(registerRequest.getOrganizationId());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setOfficeLocation(registerRequest.getOfficeLocation());
        user.setExpertiseAreas(registerRequest.getExpertiseAreas());
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Generate email verification token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));
        
        user = userRepository.save(user);
        
        // TODO: Send verification email
        
        // Generate JWT tokens
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole().name());
        
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
        
        return new AuthResponse(
            token,
            refreshToken,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            expiresAt,
            user.isEmailVerified()
        );
    }
    
    public void logout(String authHeader) {
        try {
            String token = jwtUtil.getTokenFromHeader(authHeader);
            if (token != null && jwtUtil.validateToken(token)) {
                // In a production system, you would add the token to a blacklist
                // For now, we'll just return success as the frontend will handle token removal
                // TODO: Implement token blacklisting mechanism (Redis, database, etc.)
                
                // Optional: Update user's last activity
                String username = jwtUtil.extractUsername(token);
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                }
            }
        } catch (Exception e) {
            // Even if token parsing fails, we consider logout successful
            // This prevents errors when the client tries to logout with an invalid token
        }
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        try {
            // Validate the refresh token
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                throw new InvalidOperationException("Invalid refresh token");
            }
            
            // Extract user info from refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            Long userId = jwtUtil.extractUserId(refreshToken);
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Verify user ID matches
            if (!user.getId().equals(userId)) {
                throw new InvalidOperationException("Token user mismatch");
            }
            
            if (!user.isActive()) {
                throw new InvalidOperationException("Account is deactivated");
            }
            
            // Generate new tokens
            String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), user.getRole().name());
            
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);
            
            return new AuthResponse(
                newToken,
                newRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                expiresAt,
                user.isEmailVerified()
            );
            
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid refresh token: " + e.getMessage());
        }
    }
    
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", forgotPasswordRequest.getEmail()));
        
        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        
        userRepository.save(user);
        
        // TODO: Send password reset email
        // For now, just return success
    }
    
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        // Validate password confirmation
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }
        
        User user = userRepository.findByPasswordResetToken(resetPasswordRequest.getToken())
            .orElseThrow(() -> new InvalidOperationException("Invalid reset token"));
        
        // Check if token is expired
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Reset token has expired");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }
    
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new InvalidOperationException("Invalid verification token"));
        
        // Check if token is expired
        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidOperationException("Verification token has expired");
        }
        
        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }
    

    
    /**
     * RBAC: Validate and assign appropriate role during registration
     * Allows ADMIN registration for testing purposes
     */
    private User.Role validateAndAssignRole(User.Role requestedRole) {
        System.out.println("DEBUG: validateAndAssignRole called with: " + requestedRole);
        
        // Define allowed roles for public registration (including ADMIN for testing)
        Set<User.Role> allowedPublicRoles = Set.of(
            User.Role.ADMIN,                    // Added for testing
            User.Role.PRINCIPAL_INVESTIGATOR,
            User.Role.REVIEWER,
            User.Role.PROJECT_MANAGER,          // Added for testing
            User.Role.COMMITTEE_CHAIR,          // Added for testing
            User.Role.DEPARTMENT_HEAD,          // Added for testing
            User.Role.FINANCIAL_OFFICER,        // Added for testing
            User.Role.STAKEHOLDER              // Added for testing
        );
        
        System.out.println("DEBUG: Allowed roles: " + allowedPublicRoles);
        
        // If no role specified, assign default
        if (requestedRole == null) {
            System.out.println("DEBUG: No role specified, returning PRINCIPAL_INVESTIGATOR");
            return User.Role.PRINCIPAL_INVESTIGATOR;
        }
        
        // Check if requested role is allowed for public registration
        if (allowedPublicRoles.contains(requestedRole)) {
            System.out.println("DEBUG: Role " + requestedRole + " is allowed, returning it");
            return requestedRole;
        }
        
        // Fallback to default if role not recognized
        System.out.println("DEBUG: Role " + requestedRole + " not recognized, returning PRINCIPAL_INVESTIGATOR");
        return User.Role.PRINCIPAL_INVESTIGATOR;
    }
    
    /**
     * RBAC: Update user role (Admin only functionality)
     * Only ADMIN users can promote others to restricted roles
     */
    public void updateUserRole(Long adminUserId, com.proposalmanagement.dto.RoleUpdateRequest roleUpdateRequest) {
        // Verify the requesting user is an admin
        User adminUser = userRepository.findById(adminUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));
        
        if (adminUser.getRole() != User.Role.ADMIN) {
            throw new InvalidOperationException("Only administrators can update user roles");
        }
        
        // Find the target user
        User targetUser = userRepository.findById(roleUpdateRequest.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", roleUpdateRequest.getUserId()));
        
        // Prevent admin from demoting themselves
        if (adminUserId.equals(roleUpdateRequest.getUserId()) && 
            roleUpdateRequest.getNewRole() != User.Role.ADMIN) {
            throw new InvalidOperationException("Administrators cannot demote themselves");
        }
        
        // Update the role
        targetUser.setRole(roleUpdateRequest.getNewRole());
        targetUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(targetUser);
        
        // TODO: Log the role change in audit log
        // TODO: Send notification to the user about role change
    }
    
    /**
     * RBAC: Get all users with pending role requests
     * (This would be used if we implement role request approval workflow)
     */
    public List<User> getUsersWithRestrictedRoleRequests() {
        // This is a placeholder for future enhancement
        // Could track users who requested restricted roles but got default instead
        return userRepository.findByRole(User.Role.PRINCIPAL_INVESTIGATOR);
    }
}