package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.regex.Pattern;

/**
 * Simple configuration class to handle database URL conversion
 * from PostgreSQL format to JDBC format
 */
@Configuration
public class DatabaseUrlConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlConfig.class);
    
    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @PostConstruct
    public void init() {
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            // Convert PostgreSQL URL format to JDBC format
            String jdbcUrl = convertToJdbcUrl(databaseUrl);
            
            // Set the converted URL as a system property so Spring can pick it up
            System.setProperty("DATABASE_URL", jdbcUrl);
            
            String maskedUrl = jdbcUrl.replaceAll(":[^:@]+@", ":****@");
            log.info("Converted database URL to JDBC format: {}", maskedUrl);
        }
    }
    
    /**
     * Converts a standard PostgreSQL URL to JDBC format
     * From: postgresql://user:password@host:port/dbname
     * To:   jdbc:postgresql://host:port/dbname?user=user&password=password
     */
    private String convertToJdbcUrl(String postgresUrl) {
        try {
            // Simple regex-based conversion
            if (postgresUrl.startsWith("postgresql://")) {
                // Extract components using regex
                Pattern pattern = Pattern.compile("postgresql://([^:]+):([^@]+)@([^/]+)/(.+)");
                java.util.regex.Matcher matcher = pattern.matcher(postgresUrl);
                
                if (matcher.matches()) {
                    String username = matcher.group(1);
                    String password = matcher.group(2);
                    String hostPort = matcher.group(3);
                    String dbName = matcher.group(4);
                    
                    return String.format("jdbc:postgresql://%s/%s?user=%s&password=%s",
                            hostPort, dbName, username, password);
                }
            }
        } catch (Exception e) {
            log.error("Error converting database URL", e);
        }
        
        // If conversion fails, return original URL with jdbc: prefix
        if (postgresUrl.startsWith("postgresql://")) {
            return "jdbc:" + postgresUrl;
        }
        return postgresUrl;
    }
}
