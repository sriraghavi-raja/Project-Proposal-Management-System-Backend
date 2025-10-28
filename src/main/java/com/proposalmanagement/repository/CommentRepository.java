package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByProposalIdAndIsActiveTrueOrderByCreatedDateDesc(Long proposalId);
    
    List<Comment> findByProjectIdAndIsActiveTrueOrderByCreatedDateDesc(Long projectId);
    
    List<Comment> findByEvaluationIdAndIsActiveTrueOrderByCreatedDateDesc(Long evaluationId);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.parentComment IS NULL AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findTopLevelCommentsByProposal(@Param("proposalId") Long proposalId);
    
    @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.parentComment IS NULL AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findTopLevelCommentsByProject(@Param("projectId") Long projectId);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isActive = true ORDER BY c.createdDate ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);
    
    List<Comment> findByAuthorIdAndIsActiveTrueOrderByCreatedDateDesc(Long authorId);
    
    @Query("SELECT c FROM Comment c WHERE c.commentType = :commentType AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByCommentType(@Param("commentType") Comment.CommentType commentType);
    
    @Query("SELECT c FROM Comment c WHERE c.isInternal = :isInternal AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByIsInternal(@Param("isInternal") Boolean isInternal);
    
    @Query("SELECT c FROM Comment c WHERE c.isResolved = :isResolved AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByResolutionStatus(@Param("isResolved") Boolean isResolved);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.commentType = :commentType AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByProposalIdAndCommentType(@Param("proposalId") Long proposalId, @Param("commentType") Comment.CommentType commentType);
    
    @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.commentType = :commentType AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByProjectIdAndCommentType(@Param("projectId") Long projectId, @Param("commentType") Comment.CommentType commentType);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.proposal.id = :proposalId AND c.isActive = true")
    Long countByProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.project.id = :projectId AND c.isActive = true")
    Long countByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId AND c.isActive = true")
    Long countByAuthorId(@Param("authorId") Long authorId);
    
    Optional<Comment> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.isResolved = false AND c.commentType IN ('ISSUE', 'QUESTION') AND c.isActive = true")
    List<Comment> findUnresolvedIssuesAndQuestionsByProposal(@Param("proposalId") Long proposalId);
    
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> searchByKeyword(@Param("keyword") String keyword);
    
    // Additional methods required by CommentService
    @Query("SELECT c FROM Comment c WHERE c.commentType = :commentType AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByCommentTypeAndIsActiveTrueOrderByCreatedDateDesc(@Param("commentType") Comment.CommentType commentType);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.isActive = true ORDER BY c.createdDate ASC")
    List<Comment> findRepliesByParentComment(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.isResolved = false AND c.isActive = true")
    List<Comment> findUnresolvedCommentsByProposal(@Param("proposalId") Long proposalId);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.isInternal = true AND c.isActive = true")
    List<Comment> findInternalCommentsByProposal(@Param("proposalId") Long proposalId);
    
    @Query("SELECT c FROM Comment c WHERE c.proposal.id = :proposalId AND c.isActive = true ORDER BY c.createdDate DESC")
    org.springframework.data.domain.Page<Comment> findByProposalIdAndIsActiveTrue(@Param("proposalId") Long proposalId, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.proposal.id = :proposalId AND c.isActive = true")
    Long countByProposalIdAndIsActiveTrue(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.proposal.id = :proposalId AND c.isResolved = false")
    Long countUnresolvedByProposal(@Param("proposalId") Long proposalId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId AND c.isActive = true")
    Long countByAuthorIdAndIsActiveTrue(@Param("authorId") Long authorId);
    
    @Query("SELECT c FROM Comment c WHERE c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findRecentComments(@Param("limit") int limit);
    
    @Query("SELECT c FROM Comment c WHERE c.createdDate BETWEEN :startDate AND :endDate AND c.isActive = true ORDER BY c.createdDate DESC")
    List<Comment> findByCreatedDateBetweenAndIsActiveTrueOrderByCreatedDateDesc(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}