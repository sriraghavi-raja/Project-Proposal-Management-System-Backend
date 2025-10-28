package com.proposalmanagement.controller;

import com.proposalmanagement.entity.Project;
import com.proposalmanagement.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    // Get all projects (Admin, Committee Chair, Department Head, Project Manager, Stakeholder can see all)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMITTEE_CHAIR') or hasRole('DEPARTMENT_HEAD') or " +
                  "hasRole('PROJECT_MANAGER') or hasRole('STAKEHOLDER')")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Optional<Project> project = projectService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<Project> getProjectByProposalId(@PathVariable Long proposalId) {
        Optional<Project> project = projectService.getProjectByProposalId(proposalId);
        return project.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{projectNumber}")
    public ResponseEntity<Project> getProjectByNumber(@PathVariable String projectNumber) {
        Optional<Project> project = projectService.getProjectByNumber(projectNumber);
        return project.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Create project (Admin, Committee Chair, Principal Investigator can create projects from approved proposals)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMITTEE_CHAIR') or hasRole('PRINCIPAL_INVESTIGATOR')")
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Update project (Admin, Project Manager, Principal Investigator can update)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('PRINCIPAL_INVESTIGATOR')")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project projectDetails) {
        try {
            Project updatedProject = projectService.updateProject(id, projectDetails);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Delete project (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getProjectsByStatus(@PathVariable Project.Status status) {
        List<Project> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/start-date-range")
    public ResponseEntity<List<Project>> getProjectsByStartDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Project> projects = projectService.getProjectsByStartDateRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/end-date-range")
    public ResponseEntity<List<Project>> getProjectsByEndDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Project> projects = projectService.getProjectsByEndDateRange(startDate, endDate);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/ending-before")
    public ResponseEntity<List<Project>> getActiveProjectsEndingBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Project> projects = projectService.getActiveProjectsEndingBefore(date);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/principal-investigator/{piId}")
    public ResponseEntity<List<Project>> getProjectsByPrincipalInvestigator(@PathVariable Long piId) {
        List<Project> projects = projectService.getProjectsByPrincipalInvestigator(piId);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Project>> getProjectsByDepartment(@PathVariable Long departmentId) {
        List<Project> projects = projectService.getProjectsByDepartment(departmentId);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getProjectCountByStatus(@PathVariable Project.Status status) {
        Long count = projectService.getProjectCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProjects(@RequestParam String keyword) {
        List<Project> projects = projectService.searchProjects(keyword);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/low-completion")
    public ResponseEntity<List<Project>> getActiveProjectsWithLowCompletion(@RequestParam BigDecimal percentage) {
        List<Project> projects = projectService.getActiveProjectsWithLowCompletion(percentage);
        return ResponseEntity.ok(projects);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Project> updateProjectStatus(@PathVariable Long id, @RequestBody Project.Status status) {
        try {
            Project updatedProject = projectService.updateProjectStatus(id, status);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/completion")
    public ResponseEntity<Project> updateCompletionPercentage(@PathVariable Long id, @RequestBody BigDecimal completionPercentage) {
        try {
            Project updatedProject = projectService.updateCompletionPercentage(id, completionPercentage);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/budget-utilized")
    public ResponseEntity<Project> updateBudgetUtilized(@PathVariable Long id, @RequestBody BigDecimal budgetUtilized) {
        try {
            Project updatedProject = projectService.updateBudgetUtilized(id, budgetUtilized);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/start")
    public ResponseEntity<Project> startProject(@PathVariable Long id) {
        try {
            Project startedProject = projectService.startProject(id);
            return ResponseEntity.ok(startedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<Project> completeProject(@PathVariable Long id) {
        try {
            Project completedProject = projectService.completeProject(id);
            return ResponseEntity.ok(completedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}