package com.applytrack.dto;

import com.applytrack.entity.Application;

import java.time.LocalDateTime;

public class ApplicationResponse {
    private Long id;
    private String companyName;
    private String role;
    private Application.Platform platform;
    private Application.ApplicationStatus status;
    private LocalDateTime appliedDate;
    private Application.ApplicationSource source;
    private String jobLink;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public ApplicationResponse() {}

    public ApplicationResponse(Application application) {
        this.id = application.getId();
        this.companyName = application.getCompanyName();
        this.role = application.getRole();
        this.platform = application.getPlatform();
        this.status = application.getStatus();
        this.appliedDate = application.getAppliedDate();
        this.source = application.getSource();
        this.jobLink = application.getJobLink();
        this.notes = application.getNotes();
        this.createdAt = application.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Application.Platform getPlatform() { return platform; }
    public void setPlatform(Application.Platform platform) { this.platform = platform; }

    public Application.ApplicationStatus getStatus() { return status; }
    public void setStatus(Application.ApplicationStatus status) { this.status = status; }

    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }

    public Application.ApplicationSource getSource() { return source; }
    public void setSource(Application.ApplicationSource source) { this.source = source; }

    public String getJobLink() { return jobLink; }
    public void setJobLink(String jobLink) { this.jobLink = jobLink; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
