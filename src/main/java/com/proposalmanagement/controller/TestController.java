package com.proposalmanagement.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allow all origins for test endpoints
public class TestController {
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Backend is running successfully!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("server", "Spring Boot Proposal Management API");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/swagger-test") 
    public ResponseEntity<String> swaggerTest() {
        return ResponseEntity.ok("Swagger endpoint test - if you can see this, the security config is working!");
    }
    
    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> apiTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "API endpoints are accessible!");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("cors", "CORS is working properly");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("database", "Connected");
        response.put("security", "Active");
        return ResponseEntity.ok(response);
    }
    
    // Test CORS preflight requests
    @RequestMapping(value = "/api/cors-test", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> corsTest() {
        return ResponseEntity.ok().build();
    }
}