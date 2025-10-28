package com.proposalmanagement.service;

import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.DepartmentRepository;
import com.proposalmanagement.repository.UserRepository;
import com.proposalmanagement.repository.ProposalRepository;
import com.proposalmanagement.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }
    
    public Optional<Department> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }
    
    public Optional<Department> getDepartmentByCode(String code) {
        return departmentRepository.findByCode(code);
    }
    
    public Department createDepartment(Department department) {
        // Check if department name already exists
        if (departmentRepository.findByName(department.getName()).isPresent()) {
            throw new RuntimeException("Department name already exists: " + department.getName());
        }
        
        // Check if department code already exists
        if (department.getCode() != null && 
            departmentRepository.findByCode(department.getCode()).isPresent()) {
            throw new RuntimeException("Department code already exists: " + department.getCode());
        }
        
        return departmentRepository.save(department);
    }
    
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        
        // Check name uniqueness if changed
        if (!department.getName().equals(departmentDetails.getName()) && 
            departmentRepository.findByName(departmentDetails.getName()).isPresent()) {
            throw new RuntimeException("Department name already exists: " + departmentDetails.getName());
        }
        
        // Check code uniqueness if changed
        if (departmentDetails.getCode() != null && 
            !departmentDetails.getCode().equals(department.getCode()) &&
            departmentRepository.findByCode(departmentDetails.getCode()).isPresent()) {
            throw new RuntimeException("Department code already exists: " + departmentDetails.getCode());
        }
        
        department.setName(departmentDetails.getName());
        department.setCode(departmentDetails.getCode());
        department.setDescription(departmentDetails.getDescription());
        department.setHead(departmentDetails.getHead());
        department.setLocation(departmentDetails.getLocation());
        department.setPhone(departmentDetails.getPhone());
        department.setEmail(departmentDetails.getEmail());
        department.setIsActive(departmentDetails.getIsActive());
        
        return departmentRepository.save(department);
    }
    
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        
        // Business rule: Cannot delete department with active users
        Long activeUserCount = userRepository.countActiveUsersByDepartmentName(department.getName());
        if (activeUserCount > 0) {
            throw new InvalidOperationException("Cannot delete department with " + activeUserCount + 
                " active users. Please reassign users first.");
        }
        
        // Business rule: Cannot delete department with active proposals
        Long activeProposalCount = proposalRepository.countActiveProposalsByDepartmentId(id);
        if (activeProposalCount > 0) {
            throw new InvalidOperationException("Cannot delete department with " + activeProposalCount + 
                " active proposals. Please complete or reassign proposals first.");
        }
        
        try {
            // Since we've verified no active users or proposals, we can safely delete
            departmentRepository.delete(department);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete department: " + e.getMessage(), e);
        }
    }
    
    public List<Department> getDepartmentsByHead(User head) {
        return departmentRepository.findByHead(head);
    }
    
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrue();
    }
    
    public List<Department> searchDepartments(String keyword) {
        return departmentRepository.searchByNameOrCode(keyword);
    }
    
    public Long getActiveDepartmentCount() {
        return departmentRepository.countActiveDepartments();
    }
    
    public Department activateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        department.setIsActive(true);
        return departmentRepository.save(department);
    }
    
    public Department deactivateDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        department.setIsActive(false);
        return departmentRepository.save(department);
    }
    
    public Department assignHead(Long departmentId, User head) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        department.setHead(head);
        return departmentRepository.save(department);
    }
    
    public Department removeHead(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        department.setHead(null);
        return departmentRepository.save(department);
    }
}