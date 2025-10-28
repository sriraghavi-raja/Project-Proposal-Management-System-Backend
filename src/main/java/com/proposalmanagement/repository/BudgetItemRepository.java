package com.proposalmanagement.repository;

import com.proposalmanagement.entity.BudgetItem;
import com.proposalmanagement.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {
    
    List<BudgetItem> findByProposal(Proposal proposal);
    
    List<BudgetItem> findByProposalId(Long proposalId);
    
    List<BudgetItem> findByCategory(BudgetItem.Category category);
    
    List<BudgetItem> findByProposalIdAndCategory(Long proposalId, BudgetItem.Category category);
    
    List<BudgetItem> findByYearNumber(Integer yearNumber);
    
    List<BudgetItem> findByProposalIdAndYearNumber(Long proposalId, Integer yearNumber);
    
    @Query("SELECT SUM(b.amount) FROM BudgetItem b WHERE b.proposal.id = :proposalId")
    BigDecimal getTotalBudgetByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT SUM(b.amount) FROM BudgetItem b WHERE b.proposal.id = :proposalId AND b.category = :category")
    BigDecimal getTotalBudgetByProposalIdAndCategory(@Param("proposalId") Long proposalId, 
                                                    @Param("category") BudgetItem.Category category);
    
    @Query("SELECT SUM(b.amount) FROM BudgetItem b WHERE b.proposal.id = :proposalId AND b.yearNumber = :yearNumber")
    BigDecimal getTotalBudgetByProposalIdAndYear(@Param("proposalId") Long proposalId, 
                                                @Param("yearNumber") Integer yearNumber);
    
    @Query("SELECT b.category, SUM(b.amount) FROM BudgetItem b WHERE b.proposal.id = :proposalId GROUP BY b.category")
    List<Object[]> getBudgetSummaryByCategory(@Param("proposalId") Long proposalId);
    
    @Query("SELECT b.yearNumber, SUM(b.amount) FROM BudgetItem b WHERE b.proposal.id = :proposalId GROUP BY b.yearNumber ORDER BY b.yearNumber")
    List<Object[]> getBudgetSummaryByYear(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(b) FROM BudgetItem b WHERE b.proposal.id = :proposalId")
    Long countByProposalId(@Param("proposalId") Long proposalId);
    
    // Delete methods for cascade deletion
    void deleteByProposalId(Long proposalId);
    
    void deleteByProposal(Proposal proposal);
}