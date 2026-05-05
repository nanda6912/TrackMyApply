package com.applytrack.service;

import com.applytrack.dto.ReminderRequest;
import com.applytrack.dto.ReminderResponse;
import com.applytrack.entity.Reminder;
import com.applytrack.repository.ReminderRepository;
import com.applytrack.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    public List<ReminderResponse> getRemindersByApplicationId(Long applicationId) {
        return reminderRepository.findByApplicationId(applicationId)
                .stream()
                .map(ReminderResponse::new)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getUpcomingReminders(Long userId, int daysAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(daysAhead);
        
        return reminderRepository.findUpcomingRemindersByUserId(userId, now, endDate)
                .stream()
                .map(ReminderResponse::new)
                .collect(Collectors.toList());
    }

    public List<ReminderResponse> getPendingReminders(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        return reminderRepository.findPendingRemindersByUserId(userId, now)
                .stream()
                .map(ReminderResponse::new)
                .collect(Collectors.toList());
    }

    public ReminderResponse createReminder(ReminderRequest request) {
        // Verify application exists
        if (!applicationRepository.existsById(request.getApplicationId())) {
            throw new RuntimeException("Application not found");
        }

        Reminder reminder = new Reminder(
                request.getApplicationId(),
                request.getReminderDate(),
                request.getMessage()
        );

        Reminder savedReminder = reminderRepository.save(reminder);
        return new ReminderResponse(savedReminder);
    }

    public ReminderResponse updateReminder(Long reminderId, ReminderRequest request) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));

        // Verify application exists
        if (!applicationRepository.existsById(request.getApplicationId())) {
            throw new RuntimeException("Application not found");
        }

        reminder.setApplicationId(request.getApplicationId());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setMessage(request.getMessage());

        Reminder updatedReminder = reminderRepository.save(reminder);
        return new ReminderResponse(updatedReminder);
    }

    public void deleteReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found"));
        
        reminderRepository.delete(reminder);
    }

    public void processReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pendingReminders = reminderRepository.findPendingRemindersByUserId(1L, now); // TODO: Get all users
        
        for (Reminder reminder : pendingReminders) {
            // TODO: Send notification (email, push notification, etc.)
            System.out.println("Reminder triggered: " + reminder.getMessage() + 
                             " for application ID: " + reminder.getApplicationId());
            
            // For now, we'll just log the reminder
            // In a real implementation, you would send actual notifications
        }
    }
}
