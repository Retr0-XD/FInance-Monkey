package com.financeMonkey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class FinanceMonkeyApplication {
    private static final Logger logger = LoggerFactory.getLogger(FinanceMonkeyApplication.class);
    
    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext ctx = SpringApplication.run(FinanceMonkeyApplication.class, args);
            logger.info("Finance Monkey application started successfully");
            logger.info("Active profiles: {}", String.join(", ", ctx.getEnvironment().getActiveProfiles()));
        } catch (Exception e) {
            logger.error("Failed to start Finance Monkey application", e);
        }
    }
}
