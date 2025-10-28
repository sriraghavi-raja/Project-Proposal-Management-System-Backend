package com.proposalmanagement.repository;

import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.department.name = :departmentName")
    List<User> findByDepartment(@Param("departmentName") String departmentName);
    
    List<User> findByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.department.name = :departmentName AND u.isActive = true")
    List<User> findActiveUsersByDepartment(@Param("departmentName") String departmentName);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "u.isActive = true")
    List<User> searchActiveUsers(@Param("keyword") String keyword);
    
    // Authentication-specific methods
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    // Department-related queries  
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.name = :departmentName AND u.isActive = true")
    Long countActiveUsersByDepartmentName(@Param("departmentName") String departmentName);
}