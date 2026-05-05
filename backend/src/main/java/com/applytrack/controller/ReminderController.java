package com.applytrack.controller;

import com.applytrack.dto.ReminderRequest;
import com.applytrack.dto.ReminderResponse;
import com.applytrack.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "http://localhost:3000")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    // Temporary: Using hardcoded user ID. In production, this would come from JWT token
    private static final Long CURRENT_USER_ID = 1L;

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<ReminderResponse>> getRemindersByApplication(@PathVariable Long applicationId) {
        List<ReminderResponse> reminders = reminderService.getRemindersByApplicationId(applicationId);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders(
            @RequestParam(defaultValue = "7") int daysAhead) {
        List<ReminderResponse> reminders = reminderService.getUpcomingReminders(CURRENT_USER_ID, daysAhead);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReminderResponse>> getPendingReminders() {
        List<ReminderResponse> reminders = reminderService.getPendingReminders(CURRENT_USER_ID);
        return ResponseEntity.ok(reminders);
    }

    @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request) {
        try {
            ReminderResponse reminder = reminderService.createReminder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequest request) {
        try {
            ReminderResponse reminder = reminderService.updateReminder(id, request);
            return ResponseEntity.ok(reminder);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        try {
            reminderService.deleteReminder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processReminders() {
        try {
            reminderService.processReminders();
            return ResponseEntity.ok(Map.of("message", "Reminders processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process reminders: " + e.getMessage()));
        }
    }
}
