package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for database connection retry logic
 */
@Configuration
@Profile("prod")
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
        
        // Configure retry policy - retry 10 times for specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(UnknownHostException.class, true);  // DNS resolution issues
        retryableExceptions.put(java.net.ConnectException.class, true);  // Connection refused
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(10, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        logger.info("Configured database retry template with exponential backoff");
        return retryTemplate;
    }
}
