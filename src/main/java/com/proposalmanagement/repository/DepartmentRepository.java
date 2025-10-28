package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByName(String name);
    
    Optional<Department> findByNameAndIsActiveTrue(String name);
    
    Optional<Department> findByCode(String code);
    
    Optional<Department> findByCodeAndIsActiveTrue(String code);
    
    List<Department> findByHead(User head);
    
    List<Department> findByIsActiveTrue();
    
    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Department> searchByNameOrCode(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(d) FROM Department d WHERE d.isActive = true")
    Long countActiveDepartments();
}