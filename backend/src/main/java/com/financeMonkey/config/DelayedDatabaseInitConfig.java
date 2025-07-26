package com.financeMonkey.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Configuration for database initialization
 * This class handles the delayed initialization of database resources
 * like Flyway migrations after the database becomes available
 */
@Configuration
@ConditionalOnProperty(name = "app.database.resilient-mode", havingValue = "true", matchIfMissing = true)
public class DelayedDatabaseInitConfig {

    private static final Logger log = LoggerFactory.getLogger(DelayedDatabaseInitConfig.class);

    @Autowired
    private Environment env;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired(required = false)
    private DatabaseConnectivityManager connectivityManager;
    
    @Value("${spring.flyway.enabled:false}")
    private boolean flywayEnabled;
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    @PostConstruct
    public void init() {
        log.info("Delayed database initialization configured");
        log.info("Flyway migrations will be executed when database becomes available");
    }
    
    /**
     * Scheduled task to check database connectivity and perform initialization
     * when database becomes available
     */
    @Scheduled(initialDelay = 15000, fixedDelay = 30000) // First check after 15s, then every 30s
    public void checkAndInitialize() {
        // Skip if already initialized or connectivity manager not available
        if (initialized.get() || connectivityManager == null) {
            return;
        }
        
        // Check if database is available
        if (connectivityManager.isDatabaseAvailable()) {
            log.info("Database is now available. Performing delayed initialization");
            try {
                performDelayedInitialization();
                initialized.set(true);
                log.info("Delayed database initialization completed successfully");
            } catch (Exception e) {
                log.error("Failed to perform delayed database initialization", e);
            }
        } else {
            log.debug("Database still not available. Delaying initialization");
        }
    }
    
    /**
     * Performs the actual database initialization tasks
     */
    private void performDelayedInitialization() {
        // Run Flyway migrations if enabled
        if (flywayEnabled && dataSource != null) {
            log.info("Running Flyway migrations");
            try {
                Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .baselineOnMigrate(true)
                    .load();
                
                // Execute migrations and get result object
                var migrationResult = flyway.migrate();
                log.info("Flyway migrations applied: {}", migrationResult.migrationsExecuted);
            } catch (Exception e) {
                log.error("Failed to apply Flyway migrations", e);
                // Don't throw exception - application should continue even if migrations fail
            }
        } else {
            log.info("Flyway migrations skipped: enabled={}, dataSource available={}", 
                    flywayEnabled, dataSource != null);
        }
        
        // Other initialization tasks can be added here
    }
    
    /**
     * Bean to provide Flyway for manual initialization
     * Only created if dataSource is available and not null
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
    public Flyway flyway() {
        if (dataSource == null) {
            log.warn("DataSource is null, skipping Flyway bean creation");
            return null;
        }
        
        try {
            log.info("Creating Flyway bean with existing DataSource");
            return Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .load();
        } catch (Exception e) {
            log.error("Failed to create Flyway bean: {}", e.getMessage());
            // Return null to avoid application startup failure
            return null;
        }
    }
}
