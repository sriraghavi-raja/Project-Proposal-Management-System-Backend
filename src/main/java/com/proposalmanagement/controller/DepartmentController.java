package com.proposalmanagement.controller;

import com.proposalmanagement.entity.Department;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<Department> getDepartmentByName(@PathVariable String name) {
        Optional<Department> department = departmentService.getDepartmentByName(name);
        return department.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<Department> getDepartmentByCode(@PathVariable String code) {
        Optional<Department> department = departmentService.getDepartmentByCode(code);
        return department.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        try {
            Department createdDepartment = departmentService.createDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @Valid @RequestBody Department departmentDetails) {
        try {
            Department updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Department>> getActiveDepartments() {
        List<Department> departments = departmentService.getActiveDepartments();
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Department>> searchDepartments(@RequestParam String keyword) {
        List<Department> departments = departmentService.searchDepartments(keyword);
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveDepartmentCount() {
        Long count = departmentService.getActiveDepartmentCount();
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<Department> activateDepartment(@PathVariable Long id) {
        try {
            Department activatedDepartment = departmentService.activateDepartment(id);
            return ResponseEntity.ok(activatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Department> deactivateDepartment(@PathVariable Long id) {
        try {
            Department deactivatedDepartment = departmentService.deactivateDepartment(id);
            return ResponseEntity.ok(deactivatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{departmentId}/head/{userId}")
    public ResponseEntity<Department> assignHead(@PathVariable Long departmentId, @PathVariable Long userId) {
        try {
            User head = new User();
            head.setId(userId);
            Department updatedDepartment = departmentService.assignHead(departmentId, head);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{departmentId}/head/remove")
    public ResponseEntity<Department> removeHead(@PathVariable Long departmentId) {
        try {
            Department updatedDepartment = departmentService.removeHead(departmentId);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}