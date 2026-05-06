package com.applytrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_events")
public class ApplicationEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "event_type", nullable = false)
    private ApplicationEventType eventType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    // Constructors
    public ApplicationEvent() {}
    
    public ApplicationEvent(Long applicationId, ApplicationEventType eventType, String description) {
        this.applicationId = applicationId;
        this.eventType = eventType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }
    
    public ApplicationEventType getEventType() {
        return eventType;
    }
    
    public void setEventType(ApplicationEventType eventType) {
        this.eventType = eventType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Event types enum
    public enum ApplicationEventType {
        APPLIED,
        OA,
        INTERVIEW,
        OFFER,
        REJECTED,
        STATUS_UPDATED,
        NOTE_ADDED
    }
}
