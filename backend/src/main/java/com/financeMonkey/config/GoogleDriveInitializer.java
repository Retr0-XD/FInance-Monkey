package com.financeMonkey.config;

import com.financeMonkey.service.GoogleDriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;

/**
 * Configuration to initialize Google Drive storage on application startup
 */
@Configuration
public class GoogleDriveInitializer {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveInitializer.class);
    
    private final GoogleDriveService driveService;
    private final RetryTemplate retryTemplate;
    
    @Autowired
    public GoogleDriveInitializer(GoogleDriveService driveService, RetryTemplate retryTemplate) {
        this.driveService = driveService;
        this.retryTemplate = retryTemplate;
    }
    
    /**
     * Initialize Google Drive folders when the application is ready
     * This runs asynchronously to not block application startup
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDriveFolders() {
        logger.info("Initializing Google Drive storage structure");
        
        try {
            retryTemplate.execute(context -> {
                try {
                    driveService.initializeFolders();
                    logger.info("Google Drive folders initialized successfully");
                    return true;
                } catch (Exception e) {
                    logger.warn("Failed to initialize Google Drive folders, will retry: {}", e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to initialize Google Drive folders after retries: {}", e.getMessage(), e);
            logger.info("You can manually initialize folders via the /api/drive/initialize endpoint");
        }
    }
}
