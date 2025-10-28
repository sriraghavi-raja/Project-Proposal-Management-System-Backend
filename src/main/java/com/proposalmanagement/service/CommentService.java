package com.proposalmanagement.service;

import com.proposalmanagement.entity.*;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProposalRepository proposalRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public Comment createComment(Comment comment) {
        validateComment(comment);
        
        // Validate author
        if (comment.getAuthor() == null || comment.getAuthor().getId() == null) {
            throw new InvalidOperationException("Comment author is required");
        }
        
        User author = userRepository.findById(comment.getAuthor().getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + comment.getAuthor().getId()));
        comment.setAuthor(author);
        
        // Validate associations
        validateCommentAssociations(comment);
        
        comment.setIsActive(true);
        Comment savedComment = commentRepository.save(comment);
        
        // Send notifications for new comments
        notificationService.sendCommentNotification(savedComment);
        
        return savedComment;
    }
    
    public Comment getCommentById(Long id) {
        return commentRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }
    
    public List<Comment> getCommentsByProposal(Long proposalId) {
        return commentRepository.findByProposalIdAndIsActiveTrueOrderByCreatedDateDesc(proposalId);
    }
    
    public List<Comment> getCommentsByProject(Long projectId) {
        return commentRepository.findByProjectIdAndIsActiveTrueOrderByCreatedDateDesc(projectId);
    }
    
    public List<Comment> getCommentsByEvaluation(Long evaluationId) {
        return commentRepository.findByEvaluationIdAndIsActiveTrueOrderByCreatedDateDesc(evaluationId);
    }
    
    public List<Comment> getCommentsByAuthor(Long authorId) {
        return commentRepository.findByAuthorIdAndIsActiveTrueOrderByCreatedDateDesc(authorId);
    }
    
    public List<Comment> getCommentsByType(Comment.CommentType commentType) {
        return commentRepository.findByCommentTypeAndIsActiveTrueOrderByCreatedDateDesc(commentType);
    }
    
    public List<Comment> getTopLevelComments(Long proposalId) {
        return commentRepository.findTopLevelCommentsByProposal(proposalId);
    }
    
    public List<Comment> getReplies(Long parentCommentId) {
        return commentRepository.findRepliesByParentComment(parentCommentId);
    }
    
    public List<Comment> getUnresolvedComments(Long proposalId) {
        return commentRepository.findUnresolvedCommentsByProposal(proposalId);
    }
    
    public List<Comment> getInternalComments(Long proposalId) {
        return commentRepository.findInternalCommentsByProposal(proposalId);
    }
    
    public Page<Comment> getCommentsByProposal(Long proposalId, Pageable pageable) {
        return commentRepository.findByProposalIdAndIsActiveTrue(proposalId, pageable);
    }
    
    public Comment updateComment(Long id, Comment comment) {
        Comment existingComment = getCommentById(id);
        
        // Only author can update their own comments (business rule)
        if (!existingComment.getAuthor().getId().equals(comment.getAuthor().getId())) {
            throw new InvalidOperationException("Only the author can update their own comment");
        }
        
        existingComment.setContent(comment.getContent());
        existingComment.setCommentType(comment.getCommentType());
        existingComment.setIsInternal(comment.getIsInternal());
        
        validateComment(existingComment);
        return commentRepository.save(existingComment);
    }
    
    public Comment addReply(Long parentCommentId, Comment reply) {
        Comment parentComment = getCommentById(parentCommentId);
        
        reply.setParentComment(parentComment);
        
        // Inherit the same associations as parent
        reply.setProposal(parentComment.getProposal());
        reply.setProject(parentComment.getProject());
        reply.setEvaluation(parentComment.getEvaluation());
        
        return createComment(reply);
    }
    
    public Comment resolveComment(Long commentId, Long resolvedByUserId) {
        Comment comment = getCommentById(commentId);
        
        if (comment.getIsResolved()) {
            throw new InvalidOperationException("Comment is already resolved");
        }
        
        User resolvedByUser = userRepository.findById(resolvedByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + resolvedByUserId));
        
        comment.resolve(resolvedByUser);
        Comment resolvedComment = commentRepository.save(comment);
        
        // Send resolution notification
        notificationService.sendCommentResolutionNotification(resolvedComment);
        
        return resolvedComment;
    }
    
    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setIsActive(false);
        commentRepository.save(comment);
        
        // Also deactivate all replies
        List<Comment> replies = getReplies(id);
        replies.forEach(reply -> {
            reply.setIsActive(false);
            commentRepository.save(reply);
        });
    }
    
    public Long getCommentCountByProposal(Long proposalId) {
        return commentRepository.countByProposalIdAndIsActiveTrue(proposalId);
    }
    
    public Long getUnresolvedCommentCountByProposal(Long proposalId) {
        return commentRepository.countUnresolvedByProposal(proposalId);
    }
    
    public Long getCommentCountByAuthor(Long authorId) {
        return commentRepository.countByAuthorIdAndIsActiveTrue(authorId);
    }
    
    public List<Comment> searchComments(String keyword) {
        return commentRepository.searchByKeyword(keyword);
    }
    
    public List<Comment> getRecentComments(int limit) {
        return commentRepository.findRecentComments(limit);
    }
    
    public List<Comment> getCommentsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return commentRepository.findByCreatedDateBetweenAndIsActiveTrueOrderByCreatedDateDesc(startDate, endDate);
    }
    
    private void validateComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new InvalidOperationException("Comment content is required");
        }
        
        if (comment.getContent().length() > 5000) {
            throw new InvalidOperationException("Comment content cannot exceed 5000 characters");
        }
        
        if (comment.getCommentType() == null) {
            comment.setCommentType(Comment.CommentType.GENERAL);
        }
    }
    
    private void validateCommentAssociations(Comment comment) {
        int associationCount = 0;
        
        // Validate proposal association
        if (comment.getProposal() != null) {
            if (comment.getProposal().getId() == null) {
                throw new InvalidOperationException("Invalid proposal association");
            }
            proposalRepository.findById(comment.getProposal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + comment.getProposal().getId()));
            associationCount++;
        }
        
        // Validate project association
        if (comment.getProject() != null) {
            if (comment.getProject().getId() == null) {
                throw new InvalidOperationException("Invalid project association");
            }
            projectRepository.findById(comment.getProject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + comment.getProject().getId()));
            associationCount++;
        }
        
        // Validate evaluation association
        if (comment.getEvaluation() != null) {
            if (comment.getEvaluation().getId() == null) {
                throw new InvalidOperationException("Invalid evaluation association");
            }
            evaluationRepository.findById(comment.getEvaluation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found with id: " + comment.getEvaluation().getId()));
            associationCount++;
        }
        
        // Comment must be associated with exactly one entity (proposal, project, or evaluation)
        if (associationCount != 1) {
            throw new InvalidOperationException("Comment must be associated with exactly one entity (proposal, project, or evaluation)");
        }
    }
}