package com.proposalmanagement.service;

import com.proposalmanagement.entity.BudgetItem;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.repository.BudgetItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetItemService {
    
    @Autowired
    private BudgetItemRepository budgetItemRepository;
    
    public List<BudgetItem> getAllBudgetItems() {
        return budgetItemRepository.findAll();
    }
    
    public Optional<BudgetItem> getBudgetItemById(Long id) {
        return budgetItemRepository.findById(id);
    }
    
    public BudgetItem createBudgetItem(BudgetItem budgetItem) {
        return budgetItemRepository.save(budgetItem);
    }
    
    public BudgetItem updateBudgetItem(Long id, BudgetItem budgetItemDetails) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget item not found with id: " + id));
        
        budgetItem.setProposal(budgetItemDetails.getProposal());
        budgetItem.setCategory(budgetItemDetails.getCategory());
        budgetItem.setDescription(budgetItemDetails.getDescription());
        budgetItem.setAmount(budgetItemDetails.getAmount());
        budgetItem.setJustification(budgetItemDetails.getJustification());
        budgetItem.setYearNumber(budgetItemDetails.getYearNumber());
        
        return budgetItemRepository.save(budgetItem);
    }
    
    public void deleteBudgetItem(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget item not found with id: " + id));
        budgetItemRepository.delete(budgetItem);
    }
    
    public List<BudgetItem> getBudgetItemsByProposal(Proposal proposal) {
        return budgetItemRepository.findByProposal(proposal);
    }
    
    public List<BudgetItem> getBudgetItemsByProposalId(Long proposalId) {
        return budgetItemRepository.findByProposalId(proposalId);
    }
    
    public List<BudgetItem> getBudgetItemsByCategory(BudgetItem.Category category) {
        return budgetItemRepository.findByCategory(category);
    }
    
    public List<BudgetItem> getBudgetItemsByProposalAndCategory(Long proposalId, BudgetItem.Category category) {
        return budgetItemRepository.findByProposalIdAndCategory(proposalId, category);
    }
    
    public List<BudgetItem> getBudgetItemsByYear(Integer yearNumber) {
        return budgetItemRepository.findByYearNumber(yearNumber);
    }
    
    public List<BudgetItem> getBudgetItemsByProposalAndYear(Long proposalId, Integer yearNumber) {
        return budgetItemRepository.findByProposalIdAndYearNumber(proposalId, yearNumber);
    }
    
    public BigDecimal getTotalBudgetByProposal(Long proposalId) {
        BigDecimal total = budgetItemRepository.getTotalBudgetByProposalId(proposalId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalBudgetByProposalAndCategory(Long proposalId, BudgetItem.Category category) {
        BigDecimal total = budgetItemRepository.getTotalBudgetByProposalIdAndCategory(proposalId, category);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalBudgetByProposalAndYear(Long proposalId, Integer yearNumber) {
        BigDecimal total = budgetItemRepository.getTotalBudgetByProposalIdAndYear(proposalId, yearNumber);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public List<Object[]> getBudgetSummaryByCategory(Long proposalId) {
        return budgetItemRepository.getBudgetSummaryByCategory(proposalId);
    }
    
    public List<Object[]> getBudgetSummaryByYear(Long proposalId) {
        return budgetItemRepository.getBudgetSummaryByYear(proposalId);
    }
    
    public Long getBudgetItemCountByProposal(Long proposalId) {
        return budgetItemRepository.countByProposalId(proposalId);
    }
    
    public List<BudgetItem> createMultipleBudgetItems(List<BudgetItem> budgetItems) {
        return budgetItemRepository.saveAll(budgetItems);
    }
    
    public void deleteBudgetItemsByProposal(Long proposalId) {
        List<BudgetItem> budgetItems = budgetItemRepository.findByProposalId(proposalId);
        budgetItemRepository.deleteAll(budgetItems);
    }
}