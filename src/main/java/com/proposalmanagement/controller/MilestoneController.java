package com.proposalmanagement.controller;

import com.proposalmanagement.entity.Milestone;
import com.proposalmanagement.service.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

	@Autowired
	private MilestoneService milestoneService;

	// Project Manager: create milestone for any project, PI: only for their own
	@PostMapping
	@PreAuthorize("hasRole('PROJECT_MANAGER') or (hasRole('PRINCIPAL_INVESTIGATOR') and @milestoneSecurity.isPiOfProject(#milestone.project.id))")
	public ResponseEntity<Milestone> createMilestone(@RequestBody Milestone milestone) {
		Milestone created = milestoneService.createMilestone(milestone);
		return ResponseEntity.ok(created);
	}

	// Project Manager: update any, PI: only their own
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('PROJECT_MANAGER') or (hasRole('PRINCIPAL_INVESTIGATOR') and @milestoneSecurity.isPiOfMilestone(#id))")
	public ResponseEntity<Milestone> updateMilestone(@PathVariable Long id, @RequestBody Milestone milestone) {
		Milestone updated = milestoneService.updateMilestone(id, milestone);
		return ResponseEntity.ok(updated);
	}

	// Project Manager: delete any, PI: cannot delete
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('PROJECT_MANAGER')")
	public ResponseEntity<Void> deleteMilestone(@PathVariable Long id) {
		milestoneService.deleteMilestone(id);
		return ResponseEntity.noContent().build();
	}

	// Reviewer: view milestones for assigned proposals, PI: view their own, PM: view any
	@GetMapping("/project/{projectId}")
	@PreAuthorize("hasRole('PROJECT_MANAGER') or (hasRole('PRINCIPAL_INVESTIGATOR') and @milestoneSecurity.isPiOfProject(#projectId)) or (hasRole('REVIEWER') and @milestoneSecurity.isReviewerOfProject(#projectId))")
	public ResponseEntity<List<Milestone>> getMilestonesByProject(@PathVariable Long projectId) {
		List<Milestone> milestones = milestoneService.getMilestonesByProjectId(projectId);
		return ResponseEntity.ok(milestones);
	}

	// Get single milestone (all roles with access)
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('PROJECT_MANAGER') or (hasRole('PRINCIPAL_INVESTIGATOR') and @milestoneSecurity.isPiOfMilestone(#id)) or (hasRole('REVIEWER') and @milestoneSecurity.isReviewerOfMilestone(#id))")
	public ResponseEntity<Milestone> getMilestone(@PathVariable Long id) {
		Milestone milestone = milestoneService.getMilestoneById(id);
		return ResponseEntity.ok(milestone);
	}
}
