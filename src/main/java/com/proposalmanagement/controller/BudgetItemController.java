package com.proposalmanagement.controller;

import com.proposalmanagement.entity.BudgetItem;
import com.proposalmanagement.service.BudgetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/budget-items")
@CrossOrigin(origins = "*")
public class BudgetItemController {
    
    @Autowired
    private BudgetItemService budgetItemService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE') or hasRole('COMMITTEE_CHAIR')")
    public ResponseEntity<List<BudgetItem>> getAllBudgetItems() {
        List<BudgetItem> budgetItems = budgetItemService.getAllBudgetItems();
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BudgetItem> getBudgetItemById(@PathVariable Long id) {
        Optional<BudgetItem> budgetItem = budgetItemService.getBudgetItemById(id);
        return budgetItem.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<BudgetItem> createBudgetItem(@Valid @RequestBody BudgetItem budgetItem) {
        BudgetItem createdBudgetItem = budgetItemService.createBudgetItem(budgetItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudgetItem);
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<List<BudgetItem>> createMultipleBudgetItems(@Valid @RequestBody List<BudgetItem> budgetItems) {
        List<BudgetItem> createdBudgetItems = budgetItemService.createMultipleBudgetItems(budgetItems);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudgetItems);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BudgetItem> updateBudgetItem(@PathVariable Long id, @Valid @RequestBody BudgetItem budgetItemDetails) {
        try {
            BudgetItem updatedBudgetItem = budgetItemService.updateBudgetItem(id, budgetItemDetails);
            return ResponseEntity.ok(updatedBudgetItem);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetItem(@PathVariable Long id) {
        try {
            budgetItemService.deleteBudgetItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/proposal/{proposalId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE') or hasRole('COMMITTEE_CHAIR') or hasRole('PRINCIPAL_INVESTIGATOR') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<List<BudgetItem>> getBudgetItemsByProposalId(@PathVariable Long proposalId) {
        List<BudgetItem> budgetItems = budgetItemService.getBudgetItemsByProposalId(proposalId);
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BudgetItem>> getBudgetItemsByCategory(@PathVariable BudgetItem.Category category) {
        List<BudgetItem> budgetItems = budgetItemService.getBudgetItemsByCategory(category);
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/proposal/{proposalId}/category/{category}")
    public ResponseEntity<List<BudgetItem>> getBudgetItemsByProposalAndCategory(
            @PathVariable Long proposalId, @PathVariable BudgetItem.Category category) {
        List<BudgetItem> budgetItems = budgetItemService.getBudgetItemsByProposalAndCategory(proposalId, category);
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/year/{yearNumber}")
    public ResponseEntity<List<BudgetItem>> getBudgetItemsByYear(@PathVariable Integer yearNumber) {
        List<BudgetItem> budgetItems = budgetItemService.getBudgetItemsByYear(yearNumber);
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/proposal/{proposalId}/year/{yearNumber}")
    public ResponseEntity<List<BudgetItem>> getBudgetItemsByProposalAndYear(
            @PathVariable Long proposalId, @PathVariable Integer yearNumber) {
        List<BudgetItem> budgetItems = budgetItemService.getBudgetItemsByProposalAndYear(proposalId, yearNumber);
        return ResponseEntity.ok(budgetItems);
    }
    
    @GetMapping("/proposal/{proposalId}/total")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE') or hasRole('COMMITTEE_CHAIR') or hasRole('PRINCIPAL_INVESTIGATOR') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<BigDecimal> getTotalBudgetByProposal(@PathVariable Long proposalId) {
        BigDecimal total = budgetItemService.getTotalBudgetByProposal(proposalId);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/proposal/{proposalId}/total/category/{category}")
    public ResponseEntity<BigDecimal> getTotalBudgetByProposalAndCategory(
            @PathVariable Long proposalId, @PathVariable BudgetItem.Category category) {
        BigDecimal total = budgetItemService.getTotalBudgetByProposalAndCategory(proposalId, category);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/proposal/{proposalId}/total/year/{yearNumber}")
    public ResponseEntity<BigDecimal> getTotalBudgetByProposalAndYear(
            @PathVariable Long proposalId, @PathVariable Integer yearNumber) {
        BigDecimal total = budgetItemService.getTotalBudgetByProposalAndYear(proposalId, yearNumber);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/proposal/{proposalId}/summary/category")
    public ResponseEntity<List<Object[]>> getBudgetSummaryByCategory(@PathVariable Long proposalId) {
        List<Object[]> summary = budgetItemService.getBudgetSummaryByCategory(proposalId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/proposal/{proposalId}/summary/year")
    public ResponseEntity<List<Object[]>> getBudgetSummaryByYear(@PathVariable Long proposalId) {
        List<Object[]> summary = budgetItemService.getBudgetSummaryByYear(proposalId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/proposal/{proposalId}/count")
    public ResponseEntity<Long> getBudgetItemCountByProposal(@PathVariable Long proposalId) {
        Long count = budgetItemService.getBudgetItemCountByProposal(proposalId);
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/proposal/{proposalId}")
    public ResponseEntity<Void> deleteBudgetItemsByProposal(@PathVariable Long proposalId) {
        budgetItemService.deleteBudgetItemsByProposal(proposalId);
        return ResponseEntity.noContent().build();
    }
    
    // Finance Officer specific endpoints
    @GetMapping("/financial-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCIAL_OFFICER') or hasRole('FINANCE')")
    public ResponseEntity<List<Object[]>> getFinancialSummary() {
        // This would return a summary of all budget items across proposals
        List<BudgetItem> allBudgetItems = budgetItemService.getAllBudgetItems();
        // For now, return basic summary - can be enhanced with specific financial metrics
        return ResponseEntity.ok(budgetItemService.getBudgetSummaryByCategory(null));
    }
}