package com.applytrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReminderRequest {
    
    @NotNull(message = "Application ID is required")
    private Long applicationId;
    
    @NotNull(message = "Reminder date is required")
    private LocalDateTime reminderDate;
    
    @NotBlank(message = "Message is required")
    private String message;

    // Constructors
    public ReminderRequest() {}

    // Getters and Setters
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public LocalDateTime getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDateTime reminderDate) { this.reminderDate = reminderDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
