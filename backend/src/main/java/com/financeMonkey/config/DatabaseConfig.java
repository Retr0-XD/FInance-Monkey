package com.financeMonkey.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@Profile("prod")
@EnableRetry
@ConditionalOnProperty(name = "app.database.manual-datasource-init", havingValue = "false", matchIfMissing = true)
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL}")
    private String databaseUrl;
    
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private int connectionTimeout;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:5}")
    private int maxPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minIdle;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            logger.info("Configuring database connection with DATABASE_URL");
            String maskedUrl = databaseUrl.replaceAll(":[^:@]+@", ":****@");
            logger.info("Database URL format: {}", maskedUrl);
            
            // Connection details
            String jdbcUrl;
            String username;
            String password;
            
            // First try the URI approach (more robust)
            try {
                URI dbUri = new URI(databaseUrl);
                username = dbUri.getUserInfo().split(":")[0];
                password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort() > 0 ? dbUri.getPort() : 5432;
                String database = dbUri.getPath().substring(1);
                
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                logger.info("Successfully parsed DATABASE_URL as URI. Host: {}, Database: {}", host, database);
            } catch (URISyntaxException e) {
                logger.warn("Failed to parse DATABASE_URL as URI, falling back to regex: {}", e.getMessage());
                // Fall back to regex approach
                Pattern pattern = Pattern.compile("^(postgres(?:ql)?://)?([^:]+):([^@]+)@([^:/]+)(?::(\\d+))?/(.+)$");
                Matcher matcher = pattern.matcher(databaseUrl);
                
                if (!matcher.matches()) {
                    throw new RuntimeException("Invalid DATABASE_URL format. Expected: postgresql://username:password@host:port/database");
                }
                
                username = matcher.group(2);
                password = matcher.group(3);
                String host = matcher.group(4);
                String port = matcher.group(5) != null ? matcher.group(5) : "5432";
                String database = matcher.group(6);
                
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                logger.info("Parsed DATABASE_URL with regex. Host: {}, Database: {}", host, database);
            }
            
            // Create HikariCP configuration with resilient settings
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Connection pool settings
            config.setConnectionTimeout(connectionTimeout); 
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minIdle);
            config.setIdleTimeout(60000);
            config.setMaxLifetime(1800000);
            
            // Connection test query
            config.setConnectionTestQuery("SELECT 1");
            
            // Add retries
            config.setInitializationFailTimeout(60000); // 60 seconds
            config.setValidationTimeout(5000);
            
            // Additional properties
            Properties props = new Properties();
            props.setProperty("connectTimeout", "10");
            props.setProperty("socketTimeout", "20");
            props.setProperty("tcpKeepAlive", "true");
            config.setDataSourceProperties(props);
            
            logger.info("Creating HikariCP data source with connection pooling and retry");
            return new HikariDataSource(config);
        } catch (Exception e) {
            logger.error("Failed to configure database connection: {}", e.getMessage());
            
            if (e instanceof java.net.UnknownHostException) {
                logger.error("DNS resolution failed for database host. Using dummy DataSource to allow application to start.");
            } else {
                logger.error("Database connection failed. Using dummy DataSource to allow application to start.");
            }
            
            // Create a dummy datasource that will always fail but won't be null
            // This allows the application to start without throwing NPEs
            return new org.springframework.jdbc.datasource.DriverManagerDataSource() {
                @Override
                public java.sql.Connection getConnection() throws java.sql.SQLException {
                    throw new java.sql.SQLException("Database connection not available. Application running in limited mode.");
                }
                
                @Override
                public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException {
                    throw new java.sql.SQLException("Database connection not available. Application running in limited mode.");
                }
            };
        }
    }
}
