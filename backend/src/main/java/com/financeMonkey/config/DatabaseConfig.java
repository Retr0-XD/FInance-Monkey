package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@Profile("prod")
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            logger.info("Configuring database connection with DATABASE_URL");
            logger.debug("Raw DATABASE_URL format: {}", 
                    databaseUrl.replaceAll(":[^:@]+@", ":****@")); // Log URL with password masked
            
            // First try the URI approach (more robust)
            try {
                return createDataSourceFromUri();
            } catch (URISyntaxException e) {
                logger.warn("Failed to parse DATABASE_URL as URI, falling back to regex: {}", e.getMessage());
                // Fall back to regex approach if URI parsing fails
                return createDataSourceFromRegex();
            }
        } catch (Exception e) {
            logger.error("Failed to configure database connection", e);
            throw new RuntimeException("Database configuration failed", e);
        }
    }
    
    private DataSource createDataSourceFromUri() throws URISyntaxException {
        URI dbUri = new URI(databaseUrl);
        
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String host = dbUri.getHost();
        int port = dbUri.getPort() > 0 ? dbUri.getPort() : 5432;
        String database = dbUri.getPath().substring(1);
        
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        
        logger.info("Connecting to database host: {}, database: {}", host, database);
        
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
    
    private DataSource createDataSourceFromRegex() {
        // This regex handles both postgres:// and postgresql:// URL formats
        Pattern pattern = Pattern.compile("^(postgres(?:ql)?://)?([^:]+):([^@]+)@([^:/]+)(?::(\\d+))?/(.+)$");
        Matcher matcher = pattern.matcher(databaseUrl);
        
        if (!matcher.matches()) {
            throw new RuntimeException("Invalid DATABASE_URL format. Expected: postgresql://username:password@host:port/database");
        }
        
        String username = matcher.group(2);
        String password = matcher.group(3);
        String host = matcher.group(4);
        String port = matcher.group(5) != null ? matcher.group(5) : "5432"; // Default to 5432 if no port
        String database = matcher.group(6);
        
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        
        logger.info("Connecting to database host: {}, database: {}", host, database);
        
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
