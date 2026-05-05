package com.applytrack.repository;

import com.applytrack.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    List<Reminder> findByApplicationId(Long applicationId);
    
    @Query("SELECT r FROM Reminder r WHERE r.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.userId = :userId) " +
           "AND r.reminderDate BETWEEN :startDate AND :endDate " +
           "ORDER BY r.reminderDate ASC")
    List<Reminder> findUpcomingRemindersByUserId(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT r FROM Reminder r WHERE r.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.userId = :userId) " +
           "AND r.reminderDate <= :now " +
           "ORDER BY r.reminderDate ASC")
    List<Reminder> findPendingRemindersByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
}
