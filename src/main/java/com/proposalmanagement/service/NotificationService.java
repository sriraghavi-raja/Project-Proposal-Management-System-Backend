package com.proposalmanagement.service;

import com.proposalmanagement.entity.Notification;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
    
    public Notification updateNotification(Long id, Notification notificationDetails) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        notification.setTitle(notificationDetails.getTitle());
        notification.setMessage(notificationDetails.getMessage());
        notification.setType(notificationDetails.getType());
        notification.setIsRead(notificationDetails.getIsRead());
        notification.setRelatedProposal(notificationDetails.getRelatedProposal());
        notification.setRelatedProject(notificationDetails.getRelatedProject());
        
        return notificationRepository.save(notification);
    }
    
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }
    
    public List<Notification> getNotificationsByUser(User user) {
        return notificationRepository.findByUser(user);
    }
    
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }
    
    public List<Notification> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findUnreadByUserIdOrderByCreatedDateDesc(userId);
    }
    
    public List<Notification> getNotificationsByType(Notification.Type type) {
        return notificationRepository.findByType(type);
    }
    
    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    public List<Notification> getNotificationsByProposal(Long proposalId) {
        return notificationRepository.findByRelatedProposalId(proposalId);
    }
    
    public List<Notification> getNotificationsByProject(Long projectId) {
        return notificationRepository.findByRelatedProjectId(projectId);
    }
    
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadDate(LocalDateTime.now());
        }
        
        return notificationRepository.save(notification);
    }
    
    public Notification markAsUnread(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        notification.setIsRead(false);
        notification.setReadDate(null);
        
        return notificationRepository.save(notification);
    }
    
    public void markAllAsReadForUser(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        LocalDateTime now = LocalDateTime.now();
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadDate(now);
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }
    
    public void deleteAllNotificationsForUser(Long userId) {
        List<Notification> userNotifications = notificationRepository.findByUserId(userId);
        notificationRepository.deleteAll(userNotifications);
    }
    
    public void deleteReadNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        List<Notification> readNotifications = notifications.stream()
                .filter(Notification::getIsRead)
                .toList();
        notificationRepository.deleteAll(readNotifications);
    }
    
    public Notification createNotificationForUser(Long userId, String title, String message, 
                                                Notification.Type type) {
        User user = new User();
        user.setId(userId);
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        
        return notificationRepository.save(notification);
    }
    
    public List<Notification> createBulkNotifications(List<Long> userIds, String title, 
                                                     String message, Notification.Type type) {
        List<Notification> notifications = userIds.stream()
                .map(userId -> {
                    User user = new User();
                    user.setId(userId);
                    
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setTitle(title);
                    notification.setMessage(message);
                    notification.setType(type);
                    
                    return notification;
                })
                .toList();
        
        return notificationRepository.saveAll(notifications);
    }
    
    // Specific notification methods expected by other services
    public void sendCommentNotification(com.proposalmanagement.entity.Comment comment) {
        // TODO: Implement comment notification logic
        System.out.println("Sending comment notification for comment: " + comment.getId());
    }
    
    public void sendCommentResolutionNotification(com.proposalmanagement.entity.Comment comment) {
        // TODO: Implement comment resolution notification logic
        System.out.println("Sending comment resolution notification for comment: " + comment.getId());
    }
    
    public void sendWorkflowAssignmentNotification(com.proposalmanagement.entity.WorkflowState workflowState) {
        // TODO: Implement workflow assignment notification logic
        System.out.println("Sending workflow assignment notification for workflow: " + workflowState.getId());
    }
    
    public void sendWorkflowCompletionNotification(com.proposalmanagement.entity.WorkflowState workflowState) {
        // TODO: Implement workflow completion notification logic
        System.out.println("Sending workflow completion notification for workflow: " + workflowState.getId());
    }
    
    public void sendMilestoneAssignmentNotification(com.proposalmanagement.entity.Milestone milestone) {
        // TODO: Implement milestone assignment notification logic
        System.out.println("Sending milestone assignment notification for milestone: " + milestone.getId());
    }
    
    public void sendMilestoneCompletionNotification(com.proposalmanagement.entity.Milestone milestone) {
        // TODO: Implement milestone completion notification logic
        System.out.println("Sending milestone completion notification for milestone: " + milestone.getId());
    }
}