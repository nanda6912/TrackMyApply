package com.applytrack.controller;

import com.applytrack.dto.ApplicationRequest;
import com.applytrack.dto.ApplicationResponse;
import com.applytrack.entity.Application;
import com.applytrack.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:3000")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // Temporary: Using hardcoded user ID. In production, this would come from JWT token
    private static final Long CURRENT_USER_ID = 1L;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
            @RequestParam(required = false) Application.ApplicationStatus status,
            @RequestParam(required = false) Application.Platform platform) {
        
        List<ApplicationResponse> applications;
        if (status != null || platform != null) {
            applications = applicationService.getApplicationsWithFilters(CURRENT_USER_ID, status, platform);
        } else {
            applications = applicationService.getAllApplications(CURRENT_USER_ID);
        }
        
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        try {
            ApplicationResponse application = applicationService.getApplicationById(CURRENT_USER_ID, id);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponse application = applicationService.createApplication(CURRENT_USER_ID, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/extension")
    public ResponseEntity<ApplicationResponse> createApplicationFromExtension(@Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponse application = applicationService.createApplicationFromExtension(CURRENT_USER_ID, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/email")
    public ResponseEntity<ApplicationResponse> createApplicationFromEmail(@Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponse application = applicationService.createApplicationFromEmail(CURRENT_USER_ID, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(application);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id, 
            @Valid @RequestBody ApplicationRequest request) {
        try {
            ApplicationResponse application = applicationService.updateApplication(CURRENT_USER_ID, id, request);
            return ResponseEntity.ok(application);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(CURRENT_USER_ID, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getApplicationStats() {
        // TODO: Implement statistics endpoint
        return ResponseEntity.ok(Map.of(
            "totalApplications", 0,
            "applicationsByStatus", Map.of(),
            "applicationsByPlatform", Map.of()
        ));
    }
}
