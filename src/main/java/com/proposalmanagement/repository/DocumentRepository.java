package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Document;
import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByProposal(Proposal proposal);
    
    List<Document> findByProposalId(Long proposalId);
    
    List<Document> findByProject(Project project);
    
    List<Document> findByProjectId(Long projectId);
    
    List<Document> findByUploadedBy(User uploadedBy);
    
    List<Document> findByDocumentType(Document.DocumentType documentType);
    
    List<Document> findByIsActiveTrue();
    
    @Query("SELECT d FROM Document d WHERE d.proposal.id = :proposalId AND d.isActive = true")
    List<Document> findActiveDocumentsByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT d FROM Document d WHERE d.project.id = :projectId AND d.isActive = true")
    List<Document> findActiveDocumentsByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT d FROM Document d WHERE d.documentType = :documentType AND d.isActive = true")
    List<Document> findActiveDocumentsByType(@Param("documentType") Document.DocumentType documentType);
    
    @Query("SELECT d FROM Document d WHERE " +
           "LOWER(d.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> searchByFileNameOrDescription(@Param("keyword") String keyword);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.proposal.id = :proposalId AND d.isActive = true")
    Long countActiveDocumentsByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.project.id = :projectId AND d.isActive = true")
    Long countActiveDocumentsByProjectId(@Param("projectId") Long projectId);
    
    // Delete methods for cascade deletion
    void deleteByProposalId(Long proposalId);
    
    void deleteByProposal(Proposal proposal);
    
    void deleteByProjectId(Long projectId);
    
    void deleteByProject(Project project);
}