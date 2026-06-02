package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private UserDetailsImpl getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) return null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Message is required."));
        }

        // Placeholder for Gemini AI integration
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("response", "AI chat is currently under maintenance. Please try again later.");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", responseData);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<?> analyzeIssueImage(@RequestBody Map<String, Object> body) {
        String image = (String) body.get("image");
        if (image == null || image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Image data (base64) is required."));
        }

        // Placeholder for Image analysis
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("description", "Image analysis is currently under maintenance.");
        responseData.put("category", "General");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", responseData);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/extract-intent")
    public ResponseEntity<?> extractIntent(@RequestBody Map<String, Object> body) {
        String query = (String) body.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Search query is required."));
        }

        // Placeholder for Intent Extraction
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("category", "Cleaning");
        responseData.put("keywords", new String[]{query});
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", responseData);

        return ResponseEntity.ok(result);
    }
}
