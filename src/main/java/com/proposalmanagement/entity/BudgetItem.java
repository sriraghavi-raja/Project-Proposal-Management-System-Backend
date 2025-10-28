package com.proposalmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_items")
public class BudgetItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    @JsonBackReference("proposal-budgetItems")
    private Proposal proposal;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(nullable = false, length = 200)
    private String description;
    
    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    
    @Column(columnDefinition = "TEXT")
    private String justification;
    
    @Column(name = "year_number", nullable = false)
    private Integer yearNumber = 1;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    public enum Category {
        PERSONNEL, EQUIPMENT, SUPPLIES, TRAVEL, OTHER
    }
    
    // Constructors
    public BudgetItem() {}
    
    public BudgetItem(Proposal proposal, Category category, String description, BigDecimal amount) {
        this.proposal = proposal;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Proposal getProposal() {
        return proposal;
    }
    
    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getJustification() {
        return justification;
    }
    
    public void setJustification(String justification) {
        this.justification = justification;
    }
    
    public Integer getYearNumber() {
        return yearNumber;
    }
    
    public void setYearNumber(Integer yearNumber) {
        this.yearNumber = yearNumber;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}