package com.applytrack.service;

import com.applytrack.dto.ApplicationRequest;
import com.applytrack.entity.Application;
import com.applytrack.entity.EmailProcessed;
import com.applytrack.repository.EmailProcessedRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GmailService {

    private static final String APPLICATION_NAME = "ApplyTrack Gmail Integration";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Value("${gmail.credentials.file:client_secret.json}")
    private String credentialsFilePath;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EmailProcessedRepository emailProcessedRepository;

    private Gmail gmailService;

    @PostConstruct
    public void init() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            com.google.api.client.auth.oauth2.Credential credential = getCredential(HTTP_TRANSPORT);
            if (credential != null) {
                gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            } else {
                System.out.println("Gmail service not initialized - no valid credentials found");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Gmail service: " + e.getMessage());
        }
    }

    private com.google.api.client.auth.oauth2.Credential getCredential(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets
        GoogleClientSecrets clientSecrets;
        try (FileReader reader = new FileReader(credentialsFilePath)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            // Fallback to classpath resource
            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream("client_secret.json"))) {
                if (reader == null) {
                    throw new IOException("client_secret.json not found");
                }
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            }
        }

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        return new com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp(flow, 
                new LocalServerReceiver.Builder().setPort(8080).build()).authorize("user");
    }

    public void syncEmails(Long userId) {
        if (gmailService == null) {
            throw new RuntimeException("Gmail service not initialized. Please authorize Gmail first.");
        }
        
        try {
            ListMessagesResponse response = gmailService.users().messages()
                    .list("me")
                    .setQ("application received OR thank you for applying OR job application")
                    .setMaxResults(50L)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                return;
            }

            for (Message message : messages) {
                // Check if email has already been processed
                if (emailProcessedRepository.existsByEmailId(message.getId())) {
                    continue;
                }

                processMessage(userId, message);
                
                // Mark email as processed
                EmailProcessed emailProcessed = new EmailProcessed(message.getId());
                emailProcessedRepository.save(emailProcessed);
            }
        } catch (Exception e) {
            System.err.println("Error syncing emails: " + e.getMessage());
        }
    }

    private void processMessage(Long userId, Message message) throws IOException {
        Message fullMessage = gmailService.users().messages()
                .get("me", message.getId())
                .setFormat("full")
                .execute();

        String subject = getHeader(fullMessage, "Subject");
        String from = getHeader(fullMessage, "From");
        Date date = new Date(fullMessage.getInternalDate());
        String body = getMessageBody(fullMessage);

        // Extract company and role from email
        EmailData emailData = extractJobApplicationData(subject, body, from);

        if (emailData.company != null) {
            ApplicationRequest request = new ApplicationRequest();
            request.setCompanyName(emailData.company);
            request.setRole(emailData.role != null ? emailData.role : "Unknown Position");
            request.setPlatform(detectPlatform(from, subject));
            request.setAppliedDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            request.setJobLink(emailData.jobLink);
            request.setNotes("Auto-captured from email: " + subject);

            try {
                applicationService.createApplicationFromEmail(userId, request);
            } catch (Exception e) {
                // Application might already exist, log and continue
                System.err.println("Failed to create application from email: " + e.getMessage());
            }
        }
    }

    private String getHeader(Message message, String name) {
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header.getValue();
            }
        }
        return "";
    }

    private String getMessageBody(Message message) {
        StringBuilder body = new StringBuilder();
        
        if (message.getPayload().getMimeType().startsWith("text/plain")) {
            body.append(new String(Base64.getDecoder().decode(message.getPayload().getBody().getData())));
        } else if (message.getPayload().getParts() != null) {
            for (MessagePart part : message.getPayload().getParts()) {
                if (part.getMimeType().startsWith("text/plain") && part.getBody().getData() != null) {
                    body.append(new String(Base64.getDecoder().decode(part.getBody().getData())));
                }
            }
        }
        
        return body.toString();
    }

    private EmailData extractJobApplicationData(String subject, String body, String from) {
        EmailData data = new EmailData();
        
        // Extract company name from email
        data.company = extractCompany(from, subject, body);
        
        // Extract role/position
        data.role = extractRole(subject, body);
        
        // Extract job link
        data.jobLink = extractJobLink(body);
        
        return data;
    }

    private String extractCompany(String from, String subject, String body) {
        // Extract from email domain
        Pattern domainPattern = Pattern.compile("@([^.]+)\\.");
        Matcher domainMatcher = domainPattern.matcher(from);
        
        if (domainMatcher.find()) {
            String domain = domainMatcher.group(1);
            // Clean up common domain patterns
            domain = domain.replace("noreply", "").replace("careers", "").replace("jobs", "");
            if (!domain.isEmpty() && domain.length() > 2) {
                return capitalize(domain);
            }
        }
        
        // Extract from subject/body patterns
        String[] patterns = {
            "application at ([A-Za-z\\s&]+)",
            "Application for.*?at ([A-Za-z\\s&]+)",
            "Thank you for applying to ([A-Za-z\\s&]+)",
            "([A-Za-z\\s&]+) application"
        };
        
        String combinedText = (subject + " " + body).toLowerCase();
        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(combinedText);
            if (m.find()) {
                return capitalize(m.group(1).trim());
            }
        }
        
        return null;
    }

    private String extractRole(String subject, String body) {
        String[] patterns = {
            "position of ([A-Za-z\\s-]+)",
            "role of ([A-Za-z\\s-]+)",
            "Application for ([A-Za-z\\s-]+)",
            "Software Engineer",
            "Data Scientist",
            "Product Manager",
            "Frontend Developer",
            "Backend Developer",
            "Full Stack Developer"
        };
        
        String combinedText = (subject + " " + body).toLowerCase();
        for (String pattern : patterns) {
            if (pattern.contains("[A-Za-z")) {
                Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(combinedText);
                if (m.find()) {
                    return capitalize(m.group(1).trim());
                }
            } else if (combinedText.contains(pattern.toLowerCase())) {
                return pattern;
            }
        }
        
        return null;
    }

    private String extractJobLink(String body) {
        Pattern urlPattern = Pattern.compile(
            "https?://(?:[-\\w.]|(?:%[\\da-fA-F]{2}))+[/\\w\\.-]*",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(body);
        
        while (matcher.find()) {
            String url = matcher.group();
            if (url.contains("linkedin.com/jobs") || 
                url.contains("naukri.com") || 
                url.contains("indeed.com") ||
                url.contains("careers") ||
                url.contains("jobs")) {
                return url;
            }
        }
        
        return null;
    }

    private Application.Platform detectPlatform(String from, String subject) {
        String text = (from + " " + subject).toLowerCase();
        
        if (text.contains("linkedin")) return Application.Platform.LINKEDIN;
        if (text.contains("naukri")) return Application.Platform.NAUKRI;
        if (text.contains("indeed")) return Application.Platform.INDEED;
        
        return Application.Platform.OTHER;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Arrays.stream(str.split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    public String getAuthorizationUrl() throws IOException {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load client secrets
            GoogleClientSecrets clientSecrets;
            try (FileReader reader = new FileReader(credentialsFilePath)) {
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            } catch (IOException e) {
                try (InputStreamReader reader = new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("client_secret.json"))) {
                    if (reader == null) {
                        throw new IOException("client_secret.json not found");
                    }
                    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
                }
            }

            // Build flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri("http://localhost:8082/api/gmail/auth/google/callback")
                    .build();
        } catch (Exception e) {
            throw new IOException("Failed to generate authorization URL: " + e.getMessage());
        }
    }

    public void handleCallback(String code) throws IOException {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load client secrets
            GoogleClientSecrets clientSecrets;
            try (FileReader reader = new FileReader(credentialsFilePath)) {
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            } catch (IOException e) {
                try (InputStreamReader reader = new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("client_secret.json"))) {
                    if (reader == null) {
                        throw new IOException("client_secret.json not found");
                    }
                    clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
                }
            }

            // Build flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            // Exchange code for token
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri("http://localhost:8082/api/gmail/auth/google/callback")
                    .execute();

            // Create credential from token
            com.google.api.client.auth.oauth2.Credential credential = flow.createAndStoreCredential(tokenResponse, "user");
            
            // Reinitialize Gmail service with new credentials
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            throw new IOException("Failed to handle callback: " + e.getMessage());
        }
    }

    public Map<String, Object> syncAndParseEmails(Long userId) {
        Map<String, Object> result = new HashMap<>();
        int processedCount = 0;
        int newApplications = 0;
        List<String> errors = new ArrayList<>();

        try {
            if (gmailService == null) {
                throw new RuntimeException("Gmail service not initialized. Please authorize first.");
            }

            ListMessagesResponse response = gmailService.users().messages()
                    .list("me")
                    .setQ("application received OR thank you for applying OR job application OR we received your application")
                    .setMaxResults(50L)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                result.put("message", "No job application emails found");
                result.put("processed", 0);
                result.put("newApplications", 0);
                return result;
            }

            for (Message message : messages) {
                try {
                    processedCount++;
                    
                    // Check if email has already been processed
                    if (emailProcessedRepository.existsByEmailId(message.getId())) {
                        continue;
                    }

                    boolean created = processMessageAndCreateApplication(userId, message);
                    if (created) {
                        newApplications++;
                    }
                    
                    // Mark email as processed
                    EmailProcessed emailProcessed = new EmailProcessed(message.getId());
                    emailProcessedRepository.save(emailProcessed);
                    
                } catch (Exception e) {
                    errors.add("Failed to process email " + message.getId() + ": " + e.getMessage());
                }
            }

            result.put("message", "Email sync completed successfully");
            result.put("processed", processedCount);
            result.put("newApplications", newApplications);
            result.put("errors", errors);

        } catch (Exception e) {
            result.put("error", "Failed to sync emails: " + e.getMessage());
        }

        return result;
    }

    private boolean processMessageAndCreateApplication(Long userId, Message message) throws IOException {
        Message fullMessage = gmailService.users().messages()
                .get("me", message.getId())
                .setFormat("full")
                .execute();

        String subject = getHeader(fullMessage, "Subject");
        String from = getHeader(fullMessage, "From");
        Date date = new Date(fullMessage.getInternalDate());
        String body = getMessageBody(fullMessage);

        // Extract company and role from email
        EmailData emailData = extractJobApplicationData(subject, body, from);

        if (emailData.company != null) {
            ApplicationRequest request = new ApplicationRequest();
            request.setCompanyName(emailData.company);
            request.setRole(emailData.role != null ? emailData.role : "Unknown Position");
            request.setPlatform(detectPlatform(from, subject));
            request.setAppliedDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            request.setJobLink(emailData.jobLink);
            request.setNotes("Auto-captured from email: " + subject);

            try {
                applicationService.createApplicationFromEmail(userId, request);
                return true;
            } catch (Exception e) {
                // Application might already exist, log and continue
                System.err.println("Failed to create application from email: " + e.getMessage());
                return false;
            }
        }
        
        return false;
    }

    public Map<String, Object> getGmailStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean isConnected = gmailService != null;
            status.put("connected", isConnected);
            
            if (isConnected) {
                // Get last sync time
                long processedCount = emailProcessedRepository.count();
                status.put("emailsProcessed", processedCount);
                status.put("lastSync", processedCount > 0 ? "Emails have been processed" : "No emails processed yet");
            } else {
                status.put("lastSync", null);
                status.put("message", "Gmail not authorized. Please connect your Gmail account.");
            }
            
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", "Failed to get Gmail status: " + e.getMessage());
        }
        
        return status;
    }

    private static class EmailData {
        String company;
        String role;
        String jobLink;
    }
}
