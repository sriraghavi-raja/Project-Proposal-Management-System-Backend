package com.proposalmanagement.repository;

import com.proposalmanagement.entity.Notification;
import com.proposalmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUser(User user);
    
    List<Notification> findByUserId(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    List<Notification> findByType(Notification.Type type);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdDate DESC")
    List<Notification> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdDate DESC")
    List<Notification> findUnreadByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.relatedProposal.id = :proposalId")
    List<Notification> findByRelatedProposalId(@Param("proposalId") Long proposalId);
    
    @Query("SELECT n FROM Notification n WHERE n.relatedProject.id = :projectId")
    List<Notification> findByRelatedProjectId(@Param("projectId") Long projectId);
    
    // Delete methods for cascade deletion
    void deleteByRelatedProposalId(Long proposalId);
    
    void deleteByRelatedProjectId(Long projectId);
    
    void deleteByUserId(Long userId);
}