package com.financeMonkey.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * Custom DataSource configuration that creates a DataSource
 * only after the database becomes available
 */
@Configuration
@ConditionalOnProperty(name = "app.database.manual-datasource-init", havingValue = "true", matchIfMissing = false)
public class ManualDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(ManualDataSourceConfig.class);
    
    @Autowired
    private Environment env;
    
    @Value("${spring.datasource.url:#{null}}")
    private String configuredDatabaseUrl;
    
    @Value("${spring.datasource.hikari.connection-timeout:60000}")
    private long connectionTimeout;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:5}")
    private int maximumPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:1}")
    private int minimumIdle;
    
    @Value("${app.database.retry.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${app.database.retry.initial-interval:1000}")
    private long initialInterval;
    
    @Value("${app.database.retry.multiplier:2}")
    private double multiplier;
    
    @Value("${app.database.retry.max-interval:30000}")
    private long maxInterval;
    
    /**
     * Creates a DataSource bean with retry capability
     * This will be created once the database is available
     */
    @Bean(name = "manualDataSource")
    @Retryable(value = SQLException.class, 
               maxAttempts = 5, 
               backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000))
    public DataSource manualDataSource() throws SQLException {
        log.info("Creating DataSource with retry capability");
        
        String dbUrl = getDatabaseUrl();
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new SQLException("Database URL is not configured");
        }
        
        // Log the database URL (with masked password)
        String maskedUrl = dbUrl.replaceAll(":[^:@]+@", ":****@");
        log.info("Configuring DataSource with URL: {}", maskedUrl);
        
        // Extract hostname for resolution test
        String hostname = extractHostname(dbUrl);
        if (hostname != null) {
            try {
                log.info("Testing database hostname resolution: {}", hostname);
                java.net.InetAddress address = java.net.InetAddress.getByName(hostname);
                log.info("Successfully resolved database hostname to: {}", address.getHostAddress());
            } catch (Exception e) {
                throw new SQLException("Failed to resolve database hostname: " + hostname, e);
            }
        }
        
        // Configure HikariCP with resilient settings
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(TimeUnit.SECONDS.toMillis(30));
        config.setValidationTimeout(TimeUnit.SECONDS.toMillis(10));
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(5));
        config.setIdleTimeout(TimeUnit.MINUTES.toMillis(5));
        config.setKeepaliveTime(TimeUnit.MINUTES.toMillis(2));
        config.setRegisterMbeans(true);
        config.setAutoCommit(true);
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Create and return the datasource
        try {
            HikariDataSource dataSource = new HikariDataSource(config);
            
            // Validate connection
            try (java.sql.Connection conn = dataSource.getConnection()) {
                log.info("Successfully established test connection to database");
            } catch (Exception e) {
                throw new SQLException("Failed to validate database connection", e);
            }
            
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to create DataSource", e);
            throw new SQLException("Failed to create DataSource: " + e.getMessage(), e);
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
}
