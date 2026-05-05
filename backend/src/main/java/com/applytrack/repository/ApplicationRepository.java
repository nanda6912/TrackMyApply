package com.applytrack.repository;

import com.applytrack.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByUserId(Long userId);
    
    List<Application> findByUserIdAndStatus(Long userId, Application.ApplicationStatus status);
    
    List<Application> findByUserIdAndPlatform(Long userId, Application.Platform platform);
    
    List<Application> findByUserIdAndCompanyNameContainingIgnoreCase(Long userId, String companyName);
    
    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:platform IS NULL OR a.platform = :platform) " +
           "ORDER BY a.appliedDate DESC")
    List<Application> findApplicationsWithFilters(
        @Param("userId") Long userId,
        @Param("status") Application.ApplicationStatus status,
        @Param("platform") Application.Platform platform
    );
    
    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND a.appliedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.appliedDate DESC")
    List<Application> findByUserIdAndAppliedDateBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    boolean existsByUserIdAndJobLink(Long userId, String jobLink);
    
    Optional<Application> findByUserIdAndJobLink(Long userId, String jobLink);
}
