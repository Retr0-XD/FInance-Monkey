package com.financeMonkey.controller;

import com.financeMonkey.model.Transaction;
import com.financeMonkey.security.JwtTokenUtil;
import com.financeMonkey.service.DriveBackedTransactionService;
import com.financeMonkey.service.GoogleDriveService;
import com.google.api.services.drive.Drive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing Google Drive storage of transactions
 */
@RestController
@RequestMapping("/api/drive")
public class DriveStorageController {
    private static final Logger logger = LoggerFactory.getLogger(DriveStorageController.class);
    
    private final DriveBackedTransactionService driveService;
    private final JwtTokenUtil jwtTokenUtil;
    private final Drive googleDriveService;
    
    @Autowired
    public DriveStorageController(
            DriveBackedTransactionService driveService, 
            JwtTokenUtil jwtTokenUtil,
            Drive googleDriveService) {
        this.driveService = driveService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.googleDriveService = googleDriveService;
    }
    
    /**
     * Export current user's transactions to Google Drive
     */
    @PostMapping("/export/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> exportTransactions(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.extractUserId(token.replace("Bearer ", ""));
        logger.info("Exporting transactions to Google Drive for user {}", userId);
        
        try {
            String fileId = driveService.exportTransactionsForUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("fileId", fileId);
            response.put("message", "Transactions exported successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to export transactions for user {}: {}", userId, e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to export transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get latest transactions from Google Drive
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Transaction>> getTransactionsFromDrive(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.extractUserId(token.replace("Bearer ", ""));
        logger.info("Retrieving transactions from Google Drive for user {}", userId);
        
        try {
            List<Transaction> transactions = driveService.getLatestTransactionsFromDrive(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            logger.error("Failed to retrieve transactions for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
    
    /**
     * Get transaction file history
     */
    @GetMapping("/transactions/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<GoogleDriveService.FileInfo>> getTransactionHistory(@RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.extractUserId(token.replace("Bearer ", ""));
        logger.info("Retrieving transaction file history for user {}", userId);
        
        try {
            List<GoogleDriveService.FileInfo> files = driveService.getTransactionFiles(userId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction history for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
    
    /**
     * Admin endpoint to manually trigger a full data migration to Google Drive
     */
    @PostMapping("/admin/migrate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> migrateAllData() {
        logger.info("Admin requested full data migration to Google Drive");
        
        try {
            Map<String, String> results = driveService.migrateAllDataToDrive();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to migrate all data to Google Drive: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to migrate data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Check Google Drive connection status
     * Useful for validating if Drive API is working correctly
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkDriveStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Try to access Google Drive by listing one file
            var files = googleDriveService.files().list().setPageSize(1).execute();
            
            status.put("status", "connected");
            status.put("message", "Successfully connected to Google Drive API");
            status.put("availableFiles", files.getFiles().size());
            status.put("quotaInfo", googleDriveService.about().get().setFields("storageQuota").execute().getStorageQuota());
            
            return ResponseEntity.ok(status);
        } catch (IOException e) {
            logger.error("Failed to connect to Google Drive: {}", e.getMessage(), e);
            
            status.put("status", "error");
            status.put("message", "Failed to connect to Google Drive: " + e.getMessage());
            status.put("errorType", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }
    
    /**
     * Initialize Google Drive storage
     * Creates necessary folders in Drive
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> initializeDriveStorage() {
        logger.info("Initializing Google Drive storage structure");
        
        try {
            // Initialize folders in Google Drive
            driveService.getDriveService().initializeFolders();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Google Drive storage structure initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to initialize Google Drive storage: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to initialize storage: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
