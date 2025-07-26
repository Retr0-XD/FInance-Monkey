package com.financeMonkey.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Configuration to customize Hibernate properties based on database availability
 */
@Configuration
public class EntityManagerFactoryConfig {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerFactoryConfig.class);
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * Customizes Hibernate properties to disable validation when database is not available
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            try {
                // Test if database connection is available
                if (dataSource != null) {
                    try (var connection = dataSource.getConnection()) {
                        // Database connection is available, enable validation if needed
                        logger.info("Database connection available, using standard JPA configuration");
                    } catch (Exception e) {
                        // Database connection failed, disable validation
                        logger.warn("Database connection failed, disabling schema validation: {}", e.getMessage());
                        disableValidation(hibernateProperties);
                    }
                } else {
                    // No DataSource available, disable validation
                    logger.warn("No DataSource available, disabling schema validation");
                    disableValidation(hibernateProperties);
                }
            } catch (Exception e) {
                // Something went wrong, disable validation to be safe
                logger.error("Error checking database connection, disabling schema validation: {}", e.getMessage());
                disableValidation(hibernateProperties);
            }
        };
    }
    
    /**
     * Disables schema validation in Hibernate properties
     */
    private void disableValidation(Map<String, Object> properties) {
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", false);
        properties.put("hibernate.javax.persistence.schema-generation.database.action", "none");
        properties.put("hibernate.javax.persistence.validation.mode", "none");
        properties.put("hibernate.check_nullability", false);
        // Add additional properties if needed
        logger.info("Schema validation disabled for Hibernate");
    }
}
