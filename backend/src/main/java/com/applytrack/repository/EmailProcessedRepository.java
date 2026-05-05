package com.applytrack.repository;

import com.applytrack.entity.EmailProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailProcessedRepository extends JpaRepository<EmailProcessed, Long> {
    
    Optional<EmailProcessed> findByEmailId(String emailId);
    
    boolean existsByEmailId(String emailId);
}
