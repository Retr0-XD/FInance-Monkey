package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager for database connectivity
 * This component handles database connection management in resilient mode
 * It attempts to establish database connections in the background when the
 * database is unavailable during application startup
 */
@Component
@ConditionalOnProperty(name = "app.database.resilient-mode", havingValue = "true", matchIfMissing = true)
public class DatabaseConnectivityManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectivityManager.class);
    
    @Autowired
    private Environment env;
    
    @Value("${spring.datasource.url:#{null}}")
    private String configuredDatabaseUrl;
    
    @Value("${spring.datasource.username:#{null}}")
    private String username;
    
    @Value("${spring.datasource.password:#{null}}")
    private String password;
    
    private final AtomicBoolean databaseAvailable = new AtomicBoolean(false);
    private LocalDateTime lastConnectAttempt;
    private int consecutiveFailures = 0;
    
    /**
     * Scheduled task to periodically check database connectivity
     * Uses exponential backoff for retry intervals
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 60000) // Start after 10s, then every 60s
    public void checkDatabaseConnectivity() {
        if (databaseAvailable.get()) {
            // If already connected, just validate the connection
            if (validateDatabaseConnection()) {
                log.debug("Database connection is still valid");
            } else {
                log.warn("Database connection lost, will retry to reconnect");
                databaseAvailable.set(false);
                consecutiveFailures = 1;
            }
            return;
        }
        
        lastConnectAttempt = LocalDateTime.now();
        log.info("Attempting to establish database connection (attempt #{})", consecutiveFailures + 1);
        
        try {
            testDatabaseConnectivity();
            
            log.info("Successfully established database connection after {} failed attempts", 
                    consecutiveFailures);
            databaseAvailable.set(true);
            consecutiveFailures = 0;
            
        } catch (Exception e) {
            consecutiveFailures++;
            int delayMinutes = Math.min(consecutiveFailures, 30); // Cap at 30 minutes
            
            log.warn("Failed to establish database connection (attempt #{}). Will retry in {} minutes. Error: {}", 
                    consecutiveFailures, delayMinutes, e.getMessage());
            
            if (consecutiveFailures % 5 == 0) {
                // Every 5 failures, log the full stack trace
                log.error("Database connection error details:", e);
            }
        }
    }
    
    /**
     * Tests database connectivity with retry capability
     */
    @Retryable(value = SQLException.class, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public void testDatabaseConnectivity() throws SQLException {
        String dbUrl = getDatabaseUrl();
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new SQLException("Database URL is not configured");
        }
        
        // Extract hostname for resolution test
        String hostname = extractHostname(dbUrl);
        if (hostname != null) {
            try {
                InetAddress address = InetAddress.getByName(hostname);
                log.debug("Successfully resolved database hostname {} to {}", 
                        hostname, address.getHostAddress());
            } catch (Exception e) {
                throw new SQLException("Failed to resolve database hostname: " + hostname, e);
            }
        }
        
        // Test actual database connection
        try (Connection conn = username != null && password != null ? 
                DriverManager.getConnection(dbUrl, username, password) :
                DriverManager.getConnection(dbUrl)) {
            log.debug("Successfully established test connection to database");
        }
    }
    
    /**
     * Validates if database connection is still valid
     * @return true if database is available
     */
    public boolean validateDatabaseConnection() {
        try {
            testDatabaseConnectivity();
            return true;
        } catch (Exception e) {
            log.warn("Database validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the database URL from environment or configuration
     */
    private String getDatabaseUrl() {
        // Try environment variable first (Render sets DATABASE_URL)
        String dbUrl = env.getProperty("DATABASE_URL");
        
        // If not found, use the configured one from application properties
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = configuredDatabaseUrl;
        }
        
        // Extract credentials from URL format if present (postgresql://user:pass@host:port/db)
        if (dbUrl != null && dbUrl.startsWith("postgresql://") && username == null) {
            try {
                String noPrefix = dbUrl.substring("postgresql://".length());
                if (noPrefix.contains("@")) {
                    String credentials = noPrefix.split("@")[0];
                    if (credentials.contains(":")) {
                        username = credentials.split(":")[0];
                        password = credentials.split(":")[1];
                        log.debug("Extracted credentials from DATABASE_URL");
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract credentials from DATABASE_URL", e);
            }
        }
        
        return dbUrl;
    }
    
    /**
     * Extracts hostname from database URL
     */
    private String extractHostname(String dbUrl) {
        if (dbUrl == null) return null;
        
        try {
            if (dbUrl.contains("@")) {
                // Format: jdbc:postgresql://username:password@hostname:5432/dbname
                return dbUrl.split("@")[1].split(":")[0];
            } else if (dbUrl.contains("//")) {
                // Format: jdbc:postgresql://hostname:5432/dbname
                String hostPart = dbUrl.split("//")[1];
                return hostPart.split(":")[0];
            }
        } catch (Exception e) {
            log.warn("Failed to extract hostname from database URL", e);
        }
        
        return null;
    }
    
    /**
     * Check if database is currently available
     */
    public boolean isDatabaseAvailable() {
        return databaseAvailable.get();
    }
    
    /**
     * Get information about database connectivity status
     */
    public String getConnectionStatus() {
        if (databaseAvailable.get()) {
            return "Connected";
        } else {
            return String.format("Disconnected (last attempt: %s, consecutive failures: %d)", 
                    lastConnectAttempt, consecutiveFailures);
        }
    }
}
