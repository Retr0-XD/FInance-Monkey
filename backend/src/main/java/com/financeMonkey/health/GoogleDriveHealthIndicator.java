package com.financeMonkey.health;

import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Google Drive connectivity
 */
@Component
public class GoogleDriveHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveHealthIndicator.class);
    
    private final Drive driveService;
    
    @Autowired
    public GoogleDriveHealthIndicator(Drive driveService) {
        this.driveService = driveService;
    }
    
    @Override
    public Health health() {
        try {
            // Try to list files (limit to 1) to check if the API is accessible
            driveService.files().list().setPageSize(1).execute();
            
            return Health.up()
                    .withDetail("status", "Google Drive API is accessible")
                    .build();
        } catch (Exception e) {
            logger.warn("Google Drive health check failed: {}", e.getMessage());
            
            return Health.down()
                    .withDetail("status", "Google Drive API is not accessible")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
