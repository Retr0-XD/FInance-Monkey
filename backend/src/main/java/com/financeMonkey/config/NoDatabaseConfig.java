package com.financeMonkey.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnProperty(name = "app.database.enabled", havingValue = "false")
@Profile("!test")
public class NoDatabaseConfig {
    // This class allows the application to start without a database connection
    // if the app.database.enabled property is set to false
    // Useful for testing deployment without requiring database access
}
