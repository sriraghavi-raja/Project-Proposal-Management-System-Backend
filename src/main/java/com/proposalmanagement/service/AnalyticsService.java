package com.proposalmanagement.service;

import com.proposalmanagement.entity.Analytics;
import com.proposalmanagement.exception.ResourceNotFoundException;
import com.proposalmanagement.exception.InvalidOperationException;
import com.proposalmanagement.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class AnalyticsService {
    
    @Autowired
    private AnalyticsRepository analyticsRepository;
    
    public Analytics recordMetric(Analytics analytics) {
        validateAnalytics(analytics);
        analytics.setIsActive(true);
        return analyticsRepository.save(analytics);
    }
    
    public Analytics recordMetric(String metricName, Analytics.MetricType metricType, BigDecimal value) {
        Analytics analytics = new Analytics(metricName, metricType, value);
        return recordMetric(analytics);
    }
    
    public Analytics recordCountMetric(String metricName, Analytics.MetricType metricType, Long count) {
        Analytics analytics = new Analytics(metricName, metricType, count);
        return recordMetric(analytics);
    }
    
    public Analytics getAnalyticsById(Long id) {
        return analyticsRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Analytics record not found with id: " + id));
    }
    
    public List<Analytics> getMetricsByType(Analytics.MetricType metricType) {
        return analyticsRepository.findByMetricTypeAndIsActiveTrueOrderByRecordedDateDesc(metricType);
    }
    
    public List<Analytics> getMetricsByName(String metricName) {
        return analyticsRepository.findByMetricNameAndIsActiveTrueOrderByRecordedDateDesc(metricName);
    }
    
    public List<Analytics> getMetricsByEntity(String entityType, Long entityId) {
        return analyticsRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
    
    public List<Analytics> getMetricsByDepartment(Long departmentId) {
        return analyticsRepository.findByDepartmentId(departmentId);
    }
    
    public List<Analytics> getMetricsByUser(Long userId) {
        return analyticsRepository.findByUserId(userId);
    }
    
    public List<Analytics> getMetricsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByDateRange(startDate, endDate);
    }
    
    public List<Analytics> getMetricsByTypeAndDateRange(Analytics.MetricType metricType, 
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByMetricTypeAndDateRange(metricType, startDate, endDate);
    }
    
    public List<Analytics> getMetricsByPeriodRange(LocalDateTime periodStart, LocalDateTime periodEnd) {
        return analyticsRepository.findByPeriodRange(periodStart, periodEnd);
    }
    
    public Double getAverageMetricValue(String metricName, LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.getAverageMetricValue(metricName, startDate, endDate);
    }
    
    public Long getTotalCountValue(String metricName, LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.getTotalCountValue(metricName, startDate, endDate);
    }
    
    public List<String> getDistinctMetricNames(Analytics.MetricType metricType) {
        return analyticsRepository.findDistinctMetricNamesByType(metricType);
    }
    
    public List<String> getDistinctEntityTypes() {
        return analyticsRepository.findDistinctEntityTypes();
    }
    
    public List<Analytics> getMetricsByNameAndEntity(String metricName, String entityType, Long entityId) {
        return analyticsRepository.findByMetricNameAndEntity(metricName, entityType, entityId);
    }
    
    public List<Analytics> getMetricsByTypes(List<Analytics.MetricType> metricTypes) {
        return analyticsRepository.findByMetricTypesIn(metricTypes);
    }
    
    // Dashboard analytics methods
    public Map<String, Object> getDashboardAnalytics() {
        Map<String, Object> dashboard = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Proposal metrics
        dashboard.put("totalProposals", getTotalCountValue("total_proposals", startOfMonth, now));
        dashboard.put("approvedProposals", getTotalCountValue("approved_proposals", startOfMonth, now));
        dashboard.put("proposalApprovalRate", getAverageMetricValue("proposal_approval_rate", startOfMonth, now));
        
        // Project metrics
        dashboard.put("activeProjects", getTotalCountValue("active_projects", startOfMonth, now));
        dashboard.put("completedProjects", getTotalCountValue("completed_projects", startOfMonth, now));
        dashboard.put("projectCompletionRate", getAverageMetricValue("project_completion_rate", startOfMonth, now));
        
        // Financial metrics
        dashboard.put("totalBudgetAllocated", getTotalCountValue("total_budget_allocated", startOfMonth, now));
        dashboard.put("budgetUtilized", getTotalCountValue("budget_utilized", startOfMonth, now));
        
        return dashboard;
    }
    
    public Map<String, Object> getProposalAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("submissionCount", getTotalCountValue("proposal_submissions", startDate, endDate));
        analytics.put("approvalRate", getAverageMetricValue("proposal_approval_rate", startDate, endDate));
        analytics.put("averageReviewTime", getAverageMetricValue("average_review_time", startDate, endDate));
        analytics.put("successRate", getAverageMetricValue("proposal_success_rate", startDate, endDate));
        
        return analytics;
    }
    
    public Map<String, Object> getProjectAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("projectCount", getTotalCountValue("project_count", startDate, endDate));
        analytics.put("completionRate", getAverageMetricValue("project_completion_rate", startDate, endDate));
        analytics.put("onTimeCompletion", getAverageMetricValue("on_time_completion", startDate, endDate));
        analytics.put("budgetUtilization", getAverageMetricValue("budget_utilization", startDate, endDate));
        
        return analytics;
    }
    
    public Map<String, Object> getUserAnalytics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Analytics> userMetrics = analyticsRepository.findByUserId(userId);
        
        // Filter by date range if needed
        analytics.put("activityCount", userMetrics.stream()
            .filter(m -> m.getRecordedDate().isAfter(startDate) && m.getRecordedDate().isBefore(endDate))
            .count());
        
        return analytics;
    }
    
    public Map<String, Object> getDepartmentAnalytics(Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Analytics> deptMetrics = analyticsRepository.findByDepartmentId(departmentId);
        
        analytics.put("proposalCount", deptMetrics.stream()
            .filter(m -> m.getMetricName().equals("department_proposals"))
            .filter(m -> m.getRecordedDate().isAfter(startDate) && m.getRecordedDate().isBefore(endDate))
            .mapToLong(m -> m.getCountValue() != null ? m.getCountValue() : 0)
            .sum());
        
        return analytics;
    }
    
    public void recordProposalMetrics(Long proposalId, String action) {
        Analytics analytics = new Analytics();
        analytics.setMetricName("proposal_" + action.toLowerCase());
        analytics.setMetricType(Analytics.MetricType.PROPOSAL_COUNT);
        analytics.setEntityType("proposal");
        analytics.setEntityId(proposalId);
        analytics.setCountValue(1L);
        
        recordMetric(analytics);
    }
    
    public void recordProjectMetrics(Long projectId, String action, BigDecimal value) {
        Analytics analytics = new Analytics();
        analytics.setMetricName("project_" + action.toLowerCase());
        analytics.setMetricType(Analytics.MetricType.PROJECT_COUNT);
        analytics.setEntityType("project");
        analytics.setEntityId(projectId);
        analytics.setMetricValue(value);
        
        recordMetric(analytics);
    }
    
    public void recordUserActivity(Long userId, String activity) {
        Analytics analytics = new Analytics();
        analytics.setMetricName("user_" + activity.toLowerCase());
        analytics.setMetricType(Analytics.MetricType.USER_ACTIVITY);
        analytics.setUserId(userId);
        analytics.setCountValue(1L);
        
        recordMetric(analytics);
    }
    
    public void deleteAnalytics(Long id) {
        Analytics analytics = getAnalyticsById(id);
        analytics.setIsActive(false);
        analyticsRepository.save(analytics);
    }
    
    private void validateAnalytics(Analytics analytics) {
        if (analytics.getMetricName() == null || analytics.getMetricName().trim().isEmpty()) {
            throw new InvalidOperationException("Metric name is required");
        }
        
        if (analytics.getMetricType() == null) {
            throw new InvalidOperationException("Metric type is required");
        }
    }
}