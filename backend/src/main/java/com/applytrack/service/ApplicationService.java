package com.applytrack.service;

import com.applytrack.dto.ApplicationRequest;
import com.applytrack.dto.ApplicationResponse;
import com.applytrack.entity.Application;
import com.applytrack.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

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
        // Check for duplicate job link
        if (request.getJobLink() != null && !request.getJobLink().trim().isEmpty()) {
            if (applicationRepository.existsByUserIdAndJobLink(userId, request.getJobLink())) {
                throw new RuntimeException("Application with this job link already exists");
            }
        }

        Application application = new Application(
                userId,
                request.getCompanyName(),
                request.getRole(),
                request.getPlatform(),
                request.getStatus(),
                request.getAppliedDate(),
                Application.ApplicationSource.MANUAL,
                request.getJobLink(),
                request.getNotes()
        );

        Application savedApplication = applicationRepository.save(application);
        return new ApplicationResponse(savedApplication);
    }

    public ApplicationResponse updateApplication(Long userId, Long applicationId, ApplicationRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Check for duplicate job link (excluding current application)
        if (request.getJobLink() != null && !request.getJobLink().trim().isEmpty()) {
            if (!request.getJobLink().equals(application.getJobLink()) &&
                applicationRepository.existsByUserIdAndJobLink(userId, request.getJobLink())) {
                throw new RuntimeException("Application with this job link already exists");
            }
        }

        application.setCompanyName(request.getCompanyName());
        application.setRole(request.getRole());
        application.setPlatform(request.getPlatform());
        application.setStatus(request.getStatus());
        application.setAppliedDate(request.getAppliedDate());
        application.setJobLink(request.getJobLink());
        application.setNotes(request.getNotes());

        Application updatedApplication = applicationRepository.save(application);
        return new ApplicationResponse(updatedApplication);
    }

    public void deleteApplication(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        applicationRepository.delete(application);
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
}
