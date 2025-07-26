package com.financeMonkey.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status
 * This controller provides a basic health endpoint that doesn't require
 * database connectivity, making it suitable for Render health checks
 */
@RestController
@RequestMapping("/actuator")
public class HealthController {

    @Autowired
    private Environment environment;
    
    @Value("${spring.application.name:finance-monkey}")
    private String applicationName;

    /**
     * Basic health check endpoint that doesn't require database access
     * @return Simple status information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", applicationName);
        status.put("timestamp", System.currentTimeMillis());
        
        // Add system info
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("profiles", Arrays.asList(environment.getActiveProfiles()));
        
        try {
            systemInfo.put("hostname", InetAddress.getLocalHost().getHostName());
            systemInfo.put("ip", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            systemInfo.put("hostname", "unknown");
            systemInfo.put("ip", "unknown");
        }
        
        status.put("system", systemInfo);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Detailed health information including components
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", applicationName);
        info.put("timestamp", System.currentTimeMillis());
        info.put("profiles", Arrays.asList(environment.getActiveProfiles()));
        
        // Add Java and OS info
        Map<String, String> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        
        Map<String, String> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        
        Map<String, Object> system = new HashMap<>();
        system.put("java", java);
        system.put("os", os);
        
        info.put("system", system);
        
        return ResponseEntity.ok(info);
    }
}
