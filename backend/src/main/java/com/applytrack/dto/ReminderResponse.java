package com.applytrack.dto;

import com.applytrack.entity.Reminder;

import java.time.LocalDateTime;

public class ReminderResponse {
    private Long id;
    private Long applicationId;
    private LocalDateTime reminderDate;
    private String message;
    private LocalDateTime createdAt;

    // Constructors
    public ReminderResponse() {}

    public ReminderResponse(Reminder reminder) {
        this.id = reminder.getId();
        this.applicationId = reminder.getApplicationId();
        this.reminderDate = reminder.getReminderDate();
        this.message = reminder.getMessage();
        this.createdAt = reminder.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public LocalDateTime getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDateTime reminderDate) { this.reminderDate = reminderDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
