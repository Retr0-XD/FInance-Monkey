package com.financeMonkey.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ResourceUtils;

import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for Google Drive API integration
 * Provides credential management and Drive service instantiation
 */
@Configuration
public class GoogleDriveConfig {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveConfig.class);
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "classpath:credentials/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    
    @Value("${google.application.name:Finance Monkey}")
    private String applicationName;
    
    @Bean
    public Drive driveService(RetryTemplate retryTemplate) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(applicationName)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to create Google Drive service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Google Drive service", e);
        }
    }
    
    @Bean(name = "googleDriveRetryTemplate")
    public RetryTemplate googleDriveRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Set backoff policy - exponential with 2s initial delay, 20s max
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(20000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Configure retry policy for Google Drive API exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(IOException.class, true);
        retryableExceptions.put(GeneralSecurityException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(5, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Add verbose listener
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                return true;
            }

            @Override
            public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                if (throwable == null && context.getRetryCount() > 0) {
                    logger.info("Google Drive operation succeeded after {} retries", context.getRetryCount());
                }
            }

            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                logger.warn("Google Drive operation failed (attempt {}): {}", context.getRetryCount() + 1, throwable.getMessage());
            }
        });
        
        logger.info("Configured Google Drive retry template with exponential backoff");
        return retryTemplate;
    }
    
    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Try multiple sources for credentials, in order of preference
        
        // 1. First try environment variable with full JSON
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
        if (credentialsJson != null && !credentialsJson.isEmpty()) {
            logger.info("Using Google credentials from GOOGLE_CREDENTIALS_JSON environment variable");
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY, new InputStreamReader(
                            new java.io.ByteArrayInputStream(credentialsJson.getBytes())));
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
        
        // 2. Try using existing Google API client ID and secret from environment variables
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
            logger.info("Using Google credentials from GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET environment variables");
            
            // Create client secrets from individual credentials
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(clientId);
            details.setClientSecret(clientSecret);
            details.setAuthUri("https://accounts.google.com/o/oauth2/auth");
            details.setTokenUri("https://oauth2.googleapis.com/token");
            details.setRedirectUris(List.of("urn:ietf:wg:oauth:2.0:oob", "http://localhost"));
            clientSecrets.setInstalled(details);
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
        
        // 3. Finally try to load from file
        try {
            File file = ResourceUtils.getFile(CREDENTIALS_FILE_PATH);
            logger.info("Using Google credentials from file: {}", CREDENTIALS_FILE_PATH);
            
            InputStream in = new FileInputStream(file);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            
            // Build flow and trigger user authorization request
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        } catch (IOException e) {
            logger.error("No Google credentials available from any source");
            throw new IOException("Google credentials not found from any source. Please provide GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET, or GOOGLE_CREDENTIALS_JSON environment variables, or a credentials file.", e);
        }
    }
}
