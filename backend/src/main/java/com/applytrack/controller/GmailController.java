package com.applytrack.controller;

import com.applytrack.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gmail")
@CrossOrigin(origins = "http://localhost:3000")
public class GmailController {

    @Autowired
    private GmailService gmailService;

    // Temporary: Using hardcoded user ID. In production, this would come from JWT token
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncEmails() {
        try {
            gmailService.syncEmails(CURRENT_USER_ID);
            return ResponseEntity.ok(Map.of("message", "Email sync completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to sync emails: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGmailStatus() {
        // TODO: Implement Gmail connection status check
        return ResponseEntity.ok(Map.of(
            "connected", false,
            "lastSync", null,
            "message", "Gmail integration not configured"
        ));
    }
}
