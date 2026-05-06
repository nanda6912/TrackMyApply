package com.applytrack.controller;

import com.applytrack.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:3000")
public class StatsController {

    @Autowired
    private ApplicationService applicationService;

    // Temporary: Using hardcoded user ID. In production, this would come from JWT token
    private static final Long CURRENT_USER_ID = 1L;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = applicationService.getDashboardStats(CURRENT_USER_ID);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get stats: " + e.getMessage()));
        }
    }

    @GetMapping("/platform")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics() {
        try {
            Map<String, Object> analytics = applicationService.getPlatformAnalytics(CURRENT_USER_ID);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get platform analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/timeline/{applicationId}")
    public ResponseEntity<?> getApplicationTimeline(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(applicationService.getApplicationEvents(applicationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get timeline: " + e.getMessage()));
        }
    }
}
