package com.financeMonkey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeMonkey.model.Transaction;
import com.financeMonkey.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling transaction storage in Google Drive
 */
@Service
public class DriveBackedTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(DriveBackedTransactionService.class);
    
    private final GoogleDriveService driveService;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DriveBackedTransactionService(
            GoogleDriveService driveService,
            TransactionRepository transactionRepository,
            ObjectMapper objectMapper) {
        this.driveService = driveService;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Get the underlying Google Drive service
     * 
     * @return The GoogleDriveService instance
     */
    public GoogleDriveService getDriveService() {
        return driveService;
    }
    
    /**
     * Exports all transactions for a user to Google Drive
     * 
     * @param userId The ID of the user
     * @return The ID of the created file
     */
    public String exportTransactionsForUser(String userId) {
        try {
            List<Transaction> transactions = transactionRepository.findByUserId(userId);
            logger.info("Exporting {} transactions for user {}", transactions.size(), userId);
            
            String jsonContent = objectMapper.writeValueAsString(transactions);
            return driveService.uploadJsonData(jsonContent, userId, "transactions");
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize transactions for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize transactions", e);
        } catch (IOException e) {
            logger.error("Failed to export transactions for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to export transactions to Google Drive", e);
        }
    }
    
    /**
     * Retrieves the latest transactions for a user from Google Drive
     * 
     * @param userId The ID of the user
     * @return A list of transactions, or empty if none found
     */
    public List<Transaction> getLatestTransactionsFromDrive(String userId) {
        try {
            Optional<String> jsonContent = driveService.getLatestJsonData(userId, "transactions");
            
            if (jsonContent.isPresent()) {
                return objectMapper.readValue(
                        jsonContent.get(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));
            }
            
            logger.info("No transactions found in Google Drive for user {}", userId);
            return List.of();
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions from Google Drive for user {}: {}", 
                    userId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Gets metadata about transaction files for a user
     * 
     * @param userId The ID of the user
     * @return A list of file information
     */
    public List<GoogleDriveService.FileInfo> getTransactionFiles(String userId) {
        return driveService.getUserDataFiles(userId, "transactions");
    }
    
    /**
     * Scheduled task to export all users' transactions to Google Drive once a month
     */
    @Scheduled(cron = "0 0 1 1 * ?") // Run at 1am on the 1st day of each month
    public void scheduledMonthlyExport() {
        logger.info("Starting scheduled monthly export of transactions to Google Drive");
        try {
            // Get all unique user IDs from transactions
            List<String> userIds = transactionRepository.findDistinctUserIds();
            
            for (String userId : userIds) {
                try {
                    String fileId = exportTransactionsForUser(userId);
                    logger.info("Exported transactions for user {} to file {}", userId, fileId);
                } catch (Exception e) {
                    logger.error("Failed to export transactions for user {}: {}", userId, e.getMessage(), e);
                }
            }
            
            logger.info("Completed scheduled monthly export of transactions");
        } catch (Exception e) {
            logger.error("Error during scheduled transaction export: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Exports all data for initial migration to Google Drive
     * This method can be called to perform a one-time migration
     */
    public Map<String, String> migrateAllDataToDrive() {
        logger.info("Starting full data migration to Google Drive");
        Map<String, String> results = new HashMap<>();
        
        try {
            List<String> userIds = transactionRepository.findDistinctUserIds();
            
            for (String userId : userIds) {
                try {
                    String fileId = exportTransactionsForUser(userId);
                    results.put(userId, fileId);
                    logger.info("Migrated transactions for user {} to file {}", userId, fileId);
                } catch (Exception e) {
                    logger.error("Failed to migrate transactions for user {}: {}", userId, e.getMessage(), e);
                    results.put(userId, "ERROR: " + e.getMessage());
                }
            }
            
            logger.info("Completed full data migration to Google Drive");
        } catch (Exception e) {
            logger.error("Error during full data migration: {}", e.getMessage(), e);
            results.put("ERROR", e.getMessage());
        }
        
        return results;
    }
}
