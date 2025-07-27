package com.financeMonkey.service;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for interacting with Google Drive API
 */
@Service
public class GoogleDriveService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveService.class);
    
    private final Drive driveService;
    private final RetryTemplate retryTemplate;
    
    // Folder names in Google Drive
    private static final String TRANSACTIONS_FOLDER_NAME = "FinanceMonkey_Transactions";
    private static final String USER_DATA_FOLDER_NAME = "FinanceMonkey_UserData";
    
    @Autowired
    public GoogleDriveService(Drive driveService, RetryTemplate retryTemplate) {
        this.driveService = driveService;
        this.retryTemplate = retryTemplate;
    }
    
    /**
     * Creates necessary folders in Google Drive if they don't exist
     */
    public void initializeFolders() {
        try {
            ensureFolderExists(TRANSACTIONS_FOLDER_NAME);
            ensureFolderExists(USER_DATA_FOLDER_NAME);
            logger.info("Google Drive folders initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize Google Drive folders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Google Drive storage", e);
        }
    }
    
    /**
     * Ensures a folder exists in the root of Google Drive
     * 
     * @param folderName The name of the folder to create
     * @return The ID of the folder
     */
    public String ensureFolderExists(String folderName) throws IOException {
        // Check if folder already exists
        String folderId = getFolderId(folderName);
        if (folderId != null) {
            logger.debug("Folder '{}' already exists with ID: {}", folderName, folderId);
            return folderId;
        }
        
        // Create folder if it doesn't exist
        return retryTemplate.execute(context -> {
            try {
                File folderMetadata = new File();
                folderMetadata.setName(folderName);
                folderMetadata.setMimeType("application/vnd.google-apps.folder");
                
                File folder = driveService.files().create(folderMetadata)
                        .setFields("id")
                        .execute();
                
                logger.info("Created folder '{}' with ID: {}", folderName, folder.getId());
                return folder.getId();
            } catch (IOException e) {
                logger.warn("Retry attempt to create folder '{}': {}", folderName, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * Gets the ID of a folder by name
     * 
     * @param folderName The name of the folder
     * @return The ID of the folder, or null if not found
     */
    public String getFolderId(String folderName) throws IOException {
        return retryTemplate.execute(context -> {
            try {
                String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false";
                FileList result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name)")
                        .execute();
                
                List<File> files = result.getFiles();
                if (files != null && !files.isEmpty()) {
                    return files.get(0).getId();
                }
                return null;
            } catch (IOException e) {
                logger.warn("Retry attempt to get folder ID for '{}': {}", folderName, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * Upload JSON data as a file to Google Drive
     * 
     * @param jsonContent The JSON content as a string
     * @param userId The ID of the user (used in filename)
     * @param dataType The type of data (e.g., "transactions", "categories")
     * @return The ID of the created file
     */
    public String uploadJsonData(String jsonContent, String userId, String dataType) throws IOException {
        String folderName = dataType.equals("transactions") ? TRANSACTIONS_FOLDER_NAME : USER_DATA_FOLDER_NAME;
        String folderId = ensureFolderExists(folderName);
        
        // Create a timestamp for the filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s_%s.json", userId, dataType, timestamp);
        
        return retryTemplate.execute(context -> {
            try {
                // Create file metadata
                File fileMetadata = new File();
                fileMetadata.setName(fileName);
                fileMetadata.setParents(Collections.singletonList(folderId));
                
                // File content
                ByteArrayContent content = ByteArrayContent.fromString("application/json", jsonContent);
                
                // Upload file
                File uploadedFile = driveService.files().create(fileMetadata, content)
                        .setFields("id, name, webViewLink")
                        .execute();
                
                logger.info("Uploaded file '{}' with ID: {} and web link: {}", 
                        fileName, uploadedFile.getId(), uploadedFile.getWebViewLink());
                
                return uploadedFile.getId();
            } catch (IOException e) {
                logger.warn("Retry attempt to upload file '{}': {}", fileName, e.getMessage());
                throw e;
            }
        });
    }
    
    /**
     * Gets the most recent file for a user and data type
     * 
     * @param userId The ID of the user
     * @param dataType The type of data (e.g., "transactions", "categories")
     * @return The file ID and content, or empty if not found
     */
    public Optional<String> getLatestJsonData(String userId, String dataType) {
        try {
            return retryTemplate.execute(context -> {
                try {
                    String folderName = dataType.equals("transactions") ? TRANSACTIONS_FOLDER_NAME : USER_DATA_FOLDER_NAME;
                    String folderId = getFolderId(folderName);
                    
                    if (folderId == null) {
                        logger.warn("Folder '{}' not found in Google Drive", folderName);
                        return Optional.empty();
                    }
                    
                    String query = String.format("'%s' in parents and name contains '%s_%s' and mimeType='application/json' and trashed=false", 
                            folderId, userId, dataType);
                    
                    FileList result = driveService.files().list()
                            .setQ(query)
                            .setOrderBy("createdTime desc") // Most recent first
                            .setPageSize(1)
                            .setFields("files(id, name, createdTime)")
                            .execute();
                    
                    List<File> files = result.getFiles();
                    if (files == null || files.isEmpty()) {
                        logger.info("No {} data found for user {}", dataType, userId);
                        return Optional.empty();
                    }
                    
                    // Get the most recent file
                    File file = files.get(0);
                    logger.debug("Found most recent file: {} (created: {})", file.getName(), file.getCreatedTime());
                    
                    // Download the file content
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    driveService.files().get(file.getId())
                            .executeMediaAndDownloadTo(outputStream);
                    
                    String content = outputStream.toString(StandardCharsets.UTF_8.name());
                    return Optional.of(content);
                } catch (IOException e) {
                    logger.warn("Retry attempt to get latest data for user {}, type {}: {}", 
                            userId, dataType, e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to retrieve latest data for user {}, type {}: {}", 
                    userId, dataType, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Gets all data files for a user
     * 
     * @param userId The ID of the user
     * @param dataType The type of data (e.g., "transactions", "categories")
     * @return A list of file IDs and names
     */
    public List<FileInfo> getUserDataFiles(String userId, String dataType) {
        try {
            return retryTemplate.execute(context -> {
                try {
                    List<FileInfo> files = new ArrayList<>();
                    String folderName = dataType.equals("transactions") ? TRANSACTIONS_FOLDER_NAME : USER_DATA_FOLDER_NAME;
                    String folderId = getFolderId(folderName);
                    
                    if (folderId == null) {
                        logger.warn("Folder '{}' not found in Google Drive", folderName);
                        return files;
                    }
                    
                    String query = String.format("'%s' in parents and name contains '%s_%s' and mimeType='application/json' and trashed=false", 
                            folderId, userId, dataType);
                    
                    FileList result = driveService.files().list()
                            .setQ(query)
                            .setOrderBy("createdTime desc")
                            .setFields("files(id, name, createdTime, webViewLink)")
                            .execute();
                    
                    List<File> driveFiles = result.getFiles();
                    if (driveFiles != null && !driveFiles.isEmpty()) {
                        for (File file : driveFiles) {
                            files.add(new FileInfo(
                                    file.getId(),
                                    file.getName(),
                                    file.getCreatedTime().toString(),
                                    file.getWebViewLink()
                            ));
                        }
                    }
                    
                    logger.info("Found {} files for user {}, data type {}", files.size(), userId, dataType);
                    return files;
                } catch (IOException e) {
                    logger.warn("Retry attempt to list files for user {}, type {}: {}", 
                            userId, dataType, e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to list files for user {}, type {}: {}", 
                    userId, dataType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Deletes a file from Google Drive
     * 
     * @param fileId The ID of the file to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteFile(String fileId) {
        try {
            return retryTemplate.execute(context -> {
                try {
                    driveService.files().delete(fileId).execute();
                    logger.info("Deleted file with ID: {}", fileId);
                    return true;
                } catch (IOException e) {
                    logger.warn("Retry attempt to delete file {}: {}", fileId, e.getMessage());
                    throw e;
                }
            });
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", fileId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inner class to hold file information
     */
    public static class FileInfo {
        private final String id;
        private final String name;
        private final String createdTime;
        private final String webViewLink;
        
        public FileInfo(String id, String name, String createdTime, String webViewLink) {
            this.id = id;
            this.name = name;
            this.createdTime = createdTime;
            this.webViewLink = webViewLink;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getCreatedTime() {
            return createdTime;
        }
        
        public String getWebViewLink() {
            return webViewLink;
        }
    }
}
