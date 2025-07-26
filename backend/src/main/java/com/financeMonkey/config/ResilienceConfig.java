package com.financeMonkey.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration class to handle application resilience strategies
 * This enables the application to start and operate with degraded functionality
 * when external dependencies like the database are unavailable
 */
@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        boolean dbAutoConfigExcluded = environment.getProperty("spring.autoconfigure.exclude", "")
                .contains("DataSourceAutoConfiguration");
        
        if (dbAutoConfigExcluded) {
            log.warn("==========================================================");
            log.warn("APPLICATION RUNNING IN DATABASE-RESILIENT MODE");
            log.warn("Database features will be unavailable until connectivity is established");
            log.warn("==========================================================");
        }
        
        // Log active profiles
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Active profiles: {}", String.join(", ", activeProfiles));
    }

    /**
     * Conditional bean that logs when application is in database-resilient mode
     */
    @Configuration
    @ConditionalOnProperty(name = "app.database.resilient-mode", havingValue = "true")
    public static class DatabaseResilienceConfig {
        
        private static final Logger log = LoggerFactory.getLogger(DatabaseResilienceConfig.class);
        
        @PostConstruct
        public void init() {
            log.info("Database resilience mode is active - some features may be degraded");
        }
    }
}
