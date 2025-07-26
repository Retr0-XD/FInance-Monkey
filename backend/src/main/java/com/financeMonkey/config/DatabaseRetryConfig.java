package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for database connection retry logic
 * Enhanced with comprehensive retry listeners and configurable policies
 */
@Configuration
public class DatabaseRetryConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRetryConfig.class);
    
    /**
     * Creates a RetryTemplate for database operations with specific retry policies
     * Especially useful for handling DNS resolution issues in cloud environments
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Set backoff policy - exponential with 1s initial delay, 30s max
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(30000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Configure retry policy - retry for specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(UnknownHostException.class, true);        // DNS resolution issues
        retryableExceptions.put(java.net.ConnectException.class, true);   // Connection refused
        retryableExceptions.put(java.sql.SQLException.class, true);       // SQL exceptions
        // Removed JPA persistence exception as it requires additional dependency
        
        // Increase max retries for prod environment
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(15, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Add retry listeners for better logging
        retryTemplate.registerListener(new LoggingRetryListener());
        
        logger.info("Configured database retry template with exponential backoff and enhanced logging");
        return retryTemplate;
    }
    
    /**
     * Creates a more aggressive retry template for initial connectivity
     */
    @Bean(name = "aggressiveRetryTemplate")
    public RetryTemplate aggressiveRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // More aggressive backoff strategy for initial connection
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);   // Start with 500ms
        backOffPolicy.setMultiplier(1.5);        // Slower growth
        backOffPolicy.setMaxInterval(5000);      // Cap at 5s
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Configure for quick retries - useful for initial connection attempts
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(Exception.class, true);  // Retry on any exception
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(30, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Add verbose listener
        retryTemplate.registerListener(new VerboseRetryListener());
        
        logger.info("Configured aggressive retry template for initial database connectivity");
        return retryTemplate;
    }
    
    /**
     * Basic retry listener that logs retry attempts
     */
    public static class LoggingRetryListener implements RetryListener {
        private final Logger log = LoggerFactory.getLogger(LoggingRetryListener.class);

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            return true; // Always proceed with retry
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            if (context.getRetryCount() > 0) {
                log.info("Retry operation completed after {} attempts", context.getRetryCount());
            }
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            if (context.getRetryCount() == 0) {
                log.warn("Operation failed, initiating retry. Error: {}", throwable.getMessage());
            } else {
                // Log at different levels based on retry count
                if (context.getRetryCount() % 5 == 0) {
                    log.warn("Retry attempt {} failed: {}", context.getRetryCount(), throwable.getMessage());
                } else {
                    log.debug("Retry attempt {} failed: {}", context.getRetryCount(), throwable.getMessage());
                }
            }
        }
    }
    
    /**
     * More verbose retry listener with detailed error logging
     */
    public static class VerboseRetryListener implements RetryListener {
        private final Logger log = LoggerFactory.getLogger(VerboseRetryListener.class);

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            if (throwable == null) {
                log.info("Retry operation succeeded after {} attempts", context.getRetryCount());
            } else {
                log.error("Retry operation failed after {} attempts. Final error: {}", 
                        context.getRetryCount(), throwable.getMessage());
            }
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            String errorType = throwable.getClass().getSimpleName();
            
            if (context.getRetryCount() == 0) {
                log.warn("Operation failed with {} - starting retry process: {}", 
                        errorType, throwable.getMessage());
            } else {
                // Always log the first few retries
                if (context.getRetryCount() <= 3) {
                    log.warn("Retry #{} - {} - {}", context.getRetryCount(), errorType, throwable.getMessage());
                } 
                // For the rest, use different intervals based on severity
                else if (throwable instanceof UnknownHostException) {
                    // DNS issues are critical, log more often
                    if (context.getRetryCount() % 3 == 0) {
                        log.warn("DNS resolution retry #{} - {}", context.getRetryCount(), throwable.getMessage());
                    }
                } else if (context.getRetryCount() % 5 == 0) {
                    log.warn("Retry #{} - {} - {}", context.getRetryCount(), errorType, throwable.getMessage());
                } else {
                    log.debug("Retry #{} - {} - {}", context.getRetryCount(), errorType, throwable.getMessage());
                }
            }
        }
    }
}
