package com.applytrack.repository;

import com.applytrack.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, Long> {
    
    List<ApplicationEvent> findByApplicationIdOrderByTimestampAsc(Long applicationId);
    
    List<ApplicationEvent> findByApplicationIdAndEventTypeOrderByTimestampAsc(Long applicationId, ApplicationEvent.ApplicationEventType eventType);
    
    @Query("SELECT ae FROM ApplicationEvent ae WHERE ae.applicationId = :applicationId AND ae.timestamp >= :since ORDER BY ae.timestamp ASC")
    List<ApplicationEvent> findByApplicationIdSince(@Param("applicationId") Long applicationId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(ae) FROM ApplicationEvent ae WHERE ae.applicationId = :applicationId")
    long countEventsByApplicationId(@Param("applicationId") Long applicationId);
}
