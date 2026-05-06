package com.applytrack.service;

import com.applytrack.dto.ApplicationRequest;
import com.applytrack.dto.ApplicationResponse;
import com.applytrack.entity.Application;
import com.applytrack.entity.ApplicationEvent;
import com.applytrack.repository.ApplicationRepository;
import com.applytrack.repository.ApplicationEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationEventRepository applicationEventRepository;

    public List<ApplicationResponse> getAllApplications(Long userId) {
        return applicationRepository.findByUserId(userId)
                .stream()
                .map(ApplicationResponse::new)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsWithFilters(Long userId, 
                                                               Application.ApplicationStatus status,
                                                               Application.Platform platform) {
        return applicationRepository.findApplicationsWithFilters(userId, status, platform)
                .stream()
                .map(ApplicationResponse::new)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getApplicationById(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return new ApplicationResponse(application);
    }

    public ApplicationResponse createApplication(Long userId, ApplicationRequest request) {
        Application application = new Application(
                userId,
                request.getCompanyName(),
                request.getRole(),
                request.getPlatform(),
                Application.ApplicationStatus.APPLIED,
                LocalDateTime.now(),
                Application.ApplicationSource.MANUAL,
                request.getJobLink(),
                request.getNotes()
        );

        Application savedApplication = applicationRepository.save(application);
        
        // Add APPLIED event to timeline
        ApplicationEvent event = new ApplicationEvent(
                savedApplication.getId(),
                ApplicationEvent.ApplicationEventType.APPLIED,
                "Application created manually"
        );
        applicationEventRepository.save(event);
        
        return new ApplicationResponse(savedApplication);
    }

    public ApplicationResponse createApplicationFromEmail(Long userId, ApplicationRequest request) {
        Application application = new Application(
                userId,
                request.getCompanyName(),
                request.getRole(),
                request.getPlatform(),
                Application.ApplicationStatus.APPLIED,
                request.getAppliedDate(),
                Application.ApplicationSource.EMAIL,
                request.getJobLink(),
                request.getNotes()
        );

        Application savedApplication = applicationRepository.save(application);
        return new ApplicationResponse(savedApplication);
    }

    public ApplicationResponse createApplicationFromExtension(Long userId, ApplicationRequest request) {
        Application application = new Application(
                userId,
                request.getCompanyName(),
                request.getRole(),
                request.getPlatform(),
                Application.ApplicationStatus.APPLIED,
                LocalDateTime.now(),
                Application.ApplicationSource.EXTENSION,
                request.getJobLink(),
                request.getNotes()
        );

        Application savedApplication = applicationRepository.save(application);
        return new ApplicationResponse(savedApplication);
    }

    public ApplicationResponse createOrUpdateApplicationFromEmail(Long userId, ApplicationRequest request) {
        // Check if existing application exists with same company and role
        Optional<Application> existingApp = applicationRepository
                .findByUserIdAndCompanyNameAndRoleOrderByAppliedDateDesc(userId, request.getCompanyName(), request.getRole())
                .stream()
                .findFirst();
        
        if (existingApp.isPresent()) {
            Application application = existingApp.get();
            
            // Only upgrade status forward (no downgrades)
            if (shouldUpgradeStatus(application.getStatus(), request.getStatus())) {
                Application.ApplicationStatus oldStatus = application.getStatus();
                application.setStatus(request.getStatus());
                application.setUpdatedAt(LocalDateTime.now());
                
                // Add status update event to timeline
                ApplicationEvent event = new ApplicationEvent(
                        application.getId(),
                        ApplicationEvent.ApplicationEventType.STATUS_UPDATED,
                        String.format("Status updated from %s to %s via email", oldStatus, request.getStatus())
                );
                applicationEventRepository.save(event);
            }
            
            // Update notes if new information
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                String existingNotes = application.getNotes() != null ? application.getNotes() : "";
                application.setNotes(existingNotes + "\n" + request.getNotes());
            }
            
            Application updatedApplication = applicationRepository.save(application);
            return new ApplicationResponse(updatedApplication);
        } else {
            // Create new application
            Application application = new Application(
                    userId,
                    request.getCompanyName(),
                    request.getRole(),
                    request.getPlatform(),
                    request.getStatus() != null ? request.getStatus() : Application.ApplicationStatus.APPLIED,
                    request.getAppliedDate(),
                    Application.ApplicationSource.EMAIL,
                    request.getJobLink(),
                    request.getNotes()
            );

            Application savedApplication = applicationRepository.save(application);
            
            // Add APPLIED event to timeline
            ApplicationEvent event = new ApplicationEvent(
                    savedApplication.getId(),
                    ApplicationEvent.ApplicationEventType.APPLIED,
                    "Application created from email"
            );
            applicationEventRepository.save(event);
            
            return new ApplicationResponse(savedApplication);
        }
    }

    private boolean shouldUpgradeStatus(Application.ApplicationStatus currentStatus, Application.ApplicationStatus newStatus) {
        if (newStatus == null) return false;
        
        // Define status hierarchy: APPLIED < OA < INTERVIEW < OFFER
        // REJECTED is terminal, no upgrades from it
        if (currentStatus == Application.ApplicationStatus.REJECTED) return false;
        if (currentStatus == Application.ApplicationStatus.OFFER) return false;
        
        switch (currentStatus) {
            case APPLIED:
                return newStatus == Application.ApplicationStatus.OA || 
                       newStatus == Application.ApplicationStatus.INTERVIEW || 
                       newStatus == Application.ApplicationStatus.OFFER ||
                       newStatus == Application.ApplicationStatus.REJECTED;
            case OA:
                return newStatus == Application.ApplicationStatus.INTERVIEW || 
                       newStatus == Application.ApplicationStatus.OFFER ||
                       newStatus == Application.ApplicationStatus.REJECTED;
            case INTERVIEW:
                return newStatus == Application.ApplicationStatus.OFFER ||
                       newStatus == Application.ApplicationStatus.REJECTED;
            default:
                return false;
        }
    }

    public void deleteApplication(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        applicationRepository.delete(application);
    }

    // Dashboard statistics
    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Application> allApplications = applicationRepository.findByUserId(userId);
        
        // Total applications
        long totalApplications = allApplications.size();
        stats.put("totalApplications", totalApplications);
        
        // Response rate (OA + INTERVIEW + OFFER) / total
        long responses = allApplications.stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.OA || 
                              app.getStatus() == Application.ApplicationStatus.INTERVIEW || 
                              app.getStatus() == Application.ApplicationStatus.OFFER)
                .count();
        
        double responseRate = totalApplications > 0 ? (double) responses / totalApplications * 100 : 0;
        stats.put("responseRate", Math.round(responseRate * 10.0) / 10.0); // Round to 1 decimal
        
        // Interview rate (INTERVIEW + OFFER) / total
        long interviews = allApplications.stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.INTERVIEW || 
                              app.getStatus() == Application.ApplicationStatus.OFFER)
                .count();
        
        double interviewRate = totalApplications > 0 ? (double) interviews / totalApplications * 100 : 0;
        stats.put("interviewRate", Math.round(interviewRate * 10.0) / 10.0); // Round to 1 decimal
        
        // Status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            long count = allApplications.stream()
                    .filter(app -> app.getStatus() == status)
                    .count();
            statusBreakdown.put(status.toString(), count);
        }
        stats.put("statusBreakdown", statusBreakdown);
        
        return stats;
    }
    
    // Platform analytics
    public Map<String, Object> getPlatformAnalytics(Long userId) {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Application> allApplications = applicationRepository.findByUserId(userId);
        
        // Platform breakdown
        Map<String, Map<String, Object>> platformData = new HashMap<>();
        
        for (Application.Platform platform : Application.Platform.values()) {
            List<Application> platformApps = allApplications.stream()
                    .filter(app -> app.getPlatform() == platform)
                    .toList();
            
            long total = platformApps.size();
            long responses = platformApps.stream()
                    .filter(app -> app.getStatus() == Application.ApplicationStatus.OA || 
                                  app.getStatus() == Application.ApplicationStatus.INTERVIEW || 
                                  app.getStatus() == Application.ApplicationStatus.OFFER)
                    .count();
            
            Map<String, Object> platformStats = new HashMap<>();
            platformStats.put("total", total);
            platformStats.put("responses", responses);
            platformStats.put("responseRate", total > 0 ? Math.round((double) responses / total * 100 * 10.0) / 10.0 : 0);
            
            platformData.put(platform.toString(), platformStats);
        }
        
        analytics.put("platforms", platformData);
        
        // Overall platform distribution
        Map<String, Long> distribution = new HashMap<>();
        for (Application.Platform platform : Application.Platform.values()) {
            long count = allApplications.stream()
                    .filter(app -> app.getPlatform() == platform)
                    .count();
            distribution.put(platform.toString(), count);
        }
        analytics.put("distribution", distribution);
        
        return analytics;
    }
    
    // Get application events for timeline
    public List<ApplicationEvent> getApplicationEvents(Long applicationId) {
        return applicationEventRepository.findByApplicationIdOrderByTimestampAsc(applicationId);
    }
    
    // Update existing application
    public ApplicationResponse updateApplication(Long userId, Long applicationId, ApplicationRequest request) {
        try {
            // Get existing application
            Application existingApplication = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));
            
            // Verify user ownership
            if (!existingApplication.getUserId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
            
            // Update fields
            if (request.getCompanyName() != null) {
                existingApplication.setCompanyName(request.getCompanyName());
            }
            if (request.getRole() != null) {
                existingApplication.setRole(request.getRole());
            }
            if (request.getPlatform() != null) {
                existingApplication.setPlatform(request.getPlatform());
            }
            if (request.getStatus() != null) {
                existingApplication.setStatus(request.getStatus());
            }
            if (request.getJobLink() != null) {
                existingApplication.setJobLink(request.getJobLink());
            }
            if (request.getNotes() != null) {
                existingApplication.setNotes(request.getNotes());
            }
            
            existingApplication.setUpdatedAt(LocalDateTime.now());
            
            // Create timeline event for status update
            if (request.getStatus() != null && !request.getStatus().equals(existingApplication.getStatus())) {
                ApplicationEvent statusEvent = new ApplicationEvent(
                    existingApplication.getId(),
                    ApplicationEvent.ApplicationEventType.STATUS_UPDATED,
                    String.format("Status updated from %s to %s", existingApplication.getStatus(), request.getStatus())
                );
                applicationEventRepository.save(statusEvent);
            }
            
            Application updatedApplication = applicationRepository.save(existingApplication);
            return new ApplicationResponse(updatedApplication);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update application: " + e.getMessage());
        }
    }
}
