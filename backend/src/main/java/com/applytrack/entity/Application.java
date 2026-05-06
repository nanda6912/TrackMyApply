package com.applytrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    @Column(nullable = false)
    private String companyName;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Platform platform;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ApplicationStatus status;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime appliedDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ApplicationSource source;

    @Column(length = 1000)
    private String jobLink;

    @Column(length = 2000)
    private String notes;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Application() {}

    public Application(Long userId, String companyName, String role, Platform platform, 
                     ApplicationStatus status, LocalDateTime appliedDate, 
                     ApplicationSource source, String jobLink, String notes) {
        this.userId = userId;
        this.companyName = companyName;
        this.role = role;
        this.platform = platform;
        this.status = status;
        this.appliedDate = appliedDate;
        this.source = source;
        this.jobLink = jobLink;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDateTime appliedDate) { this.appliedDate = appliedDate; }

    public ApplicationSource getSource() { return source; }
    public void setSource(ApplicationSource source) { this.source = source; }

    public String getJobLink() { return jobLink; }
    public void setJobLink(String jobLink) { this.jobLink = jobLink; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum Platform {
        LINKEDIN, NAUKRI, INDEED, OTHER
    }

    public enum ApplicationStatus {
        APPLIED, OA, INTERVIEW, OFFER, REJECTED
    }

    public enum ApplicationSource {
        EMAIL, EXTENSION, MANUAL
    }
}
