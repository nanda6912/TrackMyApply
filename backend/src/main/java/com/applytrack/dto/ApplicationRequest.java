package com.applytrack.dto;

import com.applytrack.entity.Application;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ApplicationRequest {
    
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotNull(message = "Platform is required")
    private Application.Platform platform;
    
    @NotNull(message = "Status is required")
    private Application.ApplicationStatus status;
    
    @NotNull(message = "Applied date is required")
    private LocalDateTime appliedDate;
    
    private String jobLink;
    
    private String notes;

    // Constructors
    public ApplicationRequest() {}

    // Getters and Setters
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

    public String getJobLink() { return jobLink; }
    public void setJobLink(String jobLink) { this.jobLink = jobLink; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
