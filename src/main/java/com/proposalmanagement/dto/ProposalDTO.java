package com.proposalmanagement.dto;

import com.proposalmanagement.entity.Proposal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProposalDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;
    
    private String abstract_;
    
    // Frontend compatibility fields
    private String abstractText; // alias for abstract_
    
    @NotNull(message = "Principal investigator is required")
    private Long principalInvestigatorId;
    
    private String principalInvestigatorName;
    
    private String coInvestigators;
    
    // Frontend expects teamMembers as array
    private String[] teamMembers;
    
    @NotNull(message = "Project type is required")
    private String projectType;
    
    @Size(max = 200, message = "Funding agency must be less than 200 characters")
    private String fundingAgency;
    
    private BigDecimal requestedAmount;
    
    // Frontend compatibility field
    private BigDecimal requestedBudget; // alias for requestedAmount
    
    private Integer projectDurationMonths;
    
    private LocalDate submissionDeadline;
    
    // Frontend compatibility field
    private LocalDate submissionDate; // alias for submissionDeadline
    
    private String status;
    
    @Size(max = 100, message = "Project stage must be less than 100 characters")
    private String projectStage;
    
    private String priorityLevel;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String departmentName;
    
    @NotNull(message = "Created by is required")
    private Long createdById;
    
    private String createdByName;
    
    // Constructors
    public ProposalDTO() {}
    
    public ProposalDTO(Proposal proposal) {
        this.id = proposal.getId();
        this.title = proposal.getTitle();
        this.abstract_ = proposal.getAbstract_();
        this.abstractText = proposal.getAbstract_(); // Frontend compatibility
        this.projectType = proposal.getProjectType() != null ? proposal.getProjectType().toString() : null;
        this.requestedAmount = proposal.getRequestedAmount();
        this.requestedBudget = proposal.getRequestedAmount(); // Frontend compatibility
        this.submissionDeadline = proposal.getSubmissionDeadline();
        this.submissionDate = proposal.getSubmissionDeadline(); // Frontend compatibility
        this.status = proposal.getStatus() != null ? proposal.getStatus().toString() : null;
        this.priorityLevel = proposal.getPriorityLevel() != null ? proposal.getPriorityLevel().toString() : null;
        this.projectDurationMonths = proposal.getProjectDurationMonths();
        this.projectStage = proposal.getProjectStage();
        this.fundingAgency = proposal.getFundingAgency();
        this.coInvestigators = proposal.getCoInvestigators();
        
        // Parse team members from coInvestigators for frontend compatibility
        if (proposal.getCoInvestigators() != null && !proposal.getCoInvestigators().trim().isEmpty()) {
            this.teamMembers = proposal.getCoInvestigators().split(",");
            for (int i = 0; i < this.teamMembers.length; i++) {
                this.teamMembers[i] = this.teamMembers[i].trim();
            }
        } else {
            this.teamMembers = new String[0];
        }
        
        if (proposal.getPrincipalInvestigator() != null) {
            this.principalInvestigatorId = proposal.getPrincipalInvestigator().getId();
            this.principalInvestigatorName = proposal.getPrincipalInvestigator().getFirstName() + " " + 
                                          proposal.getPrincipalInvestigator().getLastName();
        }
        
        // Set department ID directly (no department entity relationship)
        this.departmentId = proposal.getDepartmentId();
        // Note: departmentName would need to be fetched separately if needed
        
        if (proposal.getCreatedBy() != null) {
            this.createdById = proposal.getCreatedBy().getId();
            this.createdByName = proposal.getCreatedBy().getFirstName() + " " + 
                                proposal.getCreatedBy().getLastName();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAbstract_() {
        return abstract_;
    }
    
    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
        this.abstractText = abstract_; // Keep in sync
    }
    
    // Frontend compatibility getter/setter
    public String getAbstractText() {
        return abstractText;
    }
    
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
        this.abstract_ = abstractText; // Keep in sync
    }
    
    // Frontend expects 'abstract' field
    public String getAbstract() {
        return abstract_;
    }
    
    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
        this.abstractText = abstract_;
    }
    
    public Long getPrincipalInvestigatorId() {
        return principalInvestigatorId;
    }
    
    public void setPrincipalInvestigatorId(Long principalInvestigatorId) {
        this.principalInvestigatorId = principalInvestigatorId;
    }
    
    public String getPrincipalInvestigatorName() {
        return principalInvestigatorName;
    }
    
    public void setPrincipalInvestigatorName(String principalInvestigatorName) {
        this.principalInvestigatorName = principalInvestigatorName;
    }
    
    public String getCoInvestigators() {
        return coInvestigators;
    }
    
    public void setCoInvestigators(String coInvestigators) {
        this.coInvestigators = coInvestigators;
    }
    
    public String getProjectType() {
        return projectType;
    }
    
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
    
    public String getFundingAgency() {
        return fundingAgency;
    }
    
    public void setFundingAgency(String fundingAgency) {
        this.fundingAgency = fundingAgency;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
        this.requestedBudget = requestedAmount; // Keep in sync
    }
    
    // Frontend compatibility getter/setter
    public BigDecimal getRequestedBudget() {
        return requestedBudget;
    }
    
    public void setRequestedBudget(BigDecimal requestedBudget) {
        this.requestedBudget = requestedBudget;
        this.requestedAmount = requestedBudget; // Keep in sync
    }
    
    public Integer getProjectDurationMonths() {
        return projectDurationMonths;
    }
    
    public void setProjectDurationMonths(Integer projectDurationMonths) {
        this.projectDurationMonths = projectDurationMonths;
    }
    
    public LocalDate getSubmissionDeadline() {
        return submissionDeadline;
    }
    
    public void setSubmissionDeadline(LocalDate submissionDeadline) {
        this.submissionDeadline = submissionDeadline;
        this.submissionDate = submissionDeadline; // Keep in sync
    }
    
    // Frontend compatibility getter/setter
    public LocalDate getSubmissionDate() {
        return submissionDate;
    }
    
    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
        this.submissionDeadline = submissionDate; // Keep in sync
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProjectStage() {
        return projectStage;
    }
    
    public void setProjectStage(String projectStage) {
        this.projectStage = projectStage;
    }
    
    public String getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public Long getCreatedById() {
        return createdById;
    }
    
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    // Frontend compatibility getter/setter for teamMembers
    public String[] getTeamMembers() {
        return teamMembers;
    }
    
    public void setTeamMembers(String[] teamMembers) {
        this.teamMembers = teamMembers;
        // Update coInvestigators to keep in sync
        if (teamMembers != null && teamMembers.length > 0) {
            this.coInvestigators = String.join(", ", teamMembers);
        } else {
            this.coInvestigators = "";
        }
    }
}