package com.financeMonkey.service;

import com.financeMonkey.service.EmailProcessingService.TransactionInfo;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

@Slf4j
@Service
public class GeminiAIService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.project}")
    private String projectId;
    
    @Value("${gemini.location}")
    private String location;
    
    private final ObjectMapper objectMapper;
    
    public GeminiAIService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Extracts transaction information from email content using the Gemini API.
     * 
     * @param emailContent The content of the email to analyze
     * @return TransactionInfo object with extracted details, or null if no transaction found
     */
    public TransactionInfo extractTransactionInfo(String emailContent) {
        log.info("Processing email content with Gemini AI");
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-gemini-api-key")) {
            log.warn("No Gemini API key configured, using basic extraction logic");
            return extractTransactionWithBasicLogic(emailContent);
        }
        
        try {
            // Set the API key as an environment variable for the VertexAI client
            System.setProperty("GOOGLE_API_KEY", apiKey);
            
            // Create prompt for Gemini
            String prompt = buildPrompt(emailContent);
            
            // Initialize Vertex AI with the Gemini model
            try (VertexAI vertexAI = new VertexAI(projectId, location)) {
                GenerativeModel model = new GenerativeModel("gemini-1.5-pro", vertexAI);
                
                // Generate a response from Gemini
                String response = model.generateContent(prompt).getCandidates().get(0).getContent().getParts().get(0).getText();
                log.debug("Gemini API response: {}", response);
                
                // Parse the JSON response
                return parseGeminiResponse(response);
            }
        } catch (Exception e) {
            log.error("Error processing with Gemini AI, falling back to basic extraction", e);
            return extractTransactionWithBasicLogic(emailContent);
        }
    }
    
    /**
     * Builds a prompt for the Gemini model to extract transaction information.
     */
    private String buildPrompt(String emailContent) {
        return "Extract financial transaction information from this email. If there's no transaction, respond with NO_TRANSACTION. " +
               "If a transaction is found, respond with a JSON object containing these fields: " +
               "transactionDate (YYYY-MM-DD format), amount (numeric), currency (3-letter code), " +
               "vendor (company name), description (brief description), recurring (true/false), " +
               "and recurrencePattern (if recurring is true).\n\nEmail content:\n" + emailContent;
    }
    
    /**
     * Parses the JSON response from Gemini API.
     */
    private TransactionInfo parseGeminiResponse(String response) throws JsonProcessingException {
        // Check if no transaction was found
        if (response.contains("NO_TRANSACTION")) {
            return null;
        }
        
        // Extract JSON from the response (in case there's any surrounding text)
        Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        if (matcher.find()) {
            String jsonStr = matcher.group();
            
            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            TransactionInfo info = new TransactionInfo();
            
            // Parse date
            if (jsonNode.has("transactionDate")) {
                String dateStr = jsonNode.get("transactionDate").asText();
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime date = LocalDateTime.parse(dateStr + "T00:00:00");
                    info.setTransactionDate(date);
                } catch (DateTimeParseException e) {
                    info.setTransactionDate(LocalDateTime.now());
                }
            } else {
                info.setTransactionDate(LocalDateTime.now());
            }
            
            // Parse amount
            if (jsonNode.has("amount")) {
                try {
                    info.setAmount(new BigDecimal(jsonNode.get("amount").asText()));
                } catch (NumberFormatException e) {
                    info.setAmount(BigDecimal.ZERO);
                }
            } else {
                info.setAmount(BigDecimal.ZERO);
            }
            
            // Parse currency
            info.setCurrency(jsonNode.has("currency") ? jsonNode.get("currency").asText() : "USD");
            
            // Parse vendor
            info.setVendor(jsonNode.has("vendor") ? jsonNode.get("vendor").asText() : "Unknown Vendor");
            
            // Parse description
            info.setDescription(jsonNode.has("description") ? jsonNode.get("description").asText() : "");
            
            // Parse recurring flag
            info.setRecurring(jsonNode.has("recurring") && jsonNode.get("recurring").asBoolean());
            
            // Parse recurrence pattern
            info.setRecurrencePattern(jsonNode.has("recurrencePattern") ? jsonNode.get("recurrencePattern").asText() : null);
            
            return info;
        }
        
        return null;
    }
    
    /**
     * Extracts transaction information using basic pattern recognition as a fallback.
     */
    private TransactionInfo extractTransactionWithBasicLogic(String emailContent) {
        // Return null if we can't detect a clear transaction
        if (!containsTransactionKeywords(emailContent)) {
            return null;
        }
        
        TransactionInfo info = new TransactionInfo();
        
        // Try to extract amount using regex
        info.setAmount(extractAmount(emailContent));
        
        // Set other fields with reasonable defaults
        info.setTransactionDate(LocalDateTime.now().minusDays(1));
        info.setCurrency("USD");
        info.setVendor(extractVendor(emailContent));
        info.setDescription(extractBriefDescription(emailContent));
        info.setRecurring(isRecurring(emailContent));
        
        if (info.isRecurring()) {
            info.setRecurrencePattern(extractRecurrencePattern(emailContent));
        }
        
        log.info("Extracted transaction using basic logic: amount={}, vendor={}", info.getAmount(), info.getVendor());
        return info;
    }
    
    private boolean containsTransactionKeywords(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("payment") || lowerContent.contains("purchase") || 
               lowerContent.contains("transaction") || lowerContent.contains("receipt") ||
               lowerContent.contains("charge") || lowerContent.contains("invoice") || 
               lowerContent.contains("order") || lowerContent.contains("confirmation") ||
               lowerContent.contains("paid") || lowerContent.contains("amount");
    }
    
    private BigDecimal extractAmount(String content) {
        // Common patterns for amounts in emails
        Pattern[] patterns = {
            Pattern.compile("\\$\\s*(\\d+\\.\\d{2})"),             // $XX.XX
            Pattern.compile("amount[^$]*\\$\\s*(\\d+\\.\\d{2})", Pattern.CASE_INSENSITIVE), // amount: $XX.XX
            Pattern.compile("total[^$]*\\$\\s*(\\d+\\.\\d{2})", Pattern.CASE_INSENSITIVE),  // total: $XX.XX
            Pattern.compile("USD\\s*(\\d+\\.\\d{2})"),            // USD XX.XX
            Pattern.compile("(\\d+\\.\\d{2})\\s*USD")             // XX.XX USD
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    return new BigDecimal(matcher.group(1));
                } catch (NumberFormatException e) {
                    // Continue to the next pattern
                }
            }
        }
        
        // Default amount if none found
        return new BigDecimal("29.99");
    }
    
    private String extractVendor(String content) {
        // Common patterns for vendor names in emails
        String[] vendorKeywords = {"from:", "merchant:", "vendor:", "store:", "seller:", "company:"};
        
        for (String keyword : vendorKeywords) {
            int index = content.toLowerCase().indexOf(keyword);
            if (index >= 0) {
                // Extract text after the keyword until a newline or period
                int start = index + keyword.length();
                int end = content.indexOf('\n', start);
                if (end == -1) end = content.indexOf('.', start);
                if (end == -1) end = start + 30; // Limit to 30 chars if no terminator found
                if (end > start) {
                    String vendor = content.substring(start, end).trim();
                    if (!vendor.isEmpty()) {
                        return vendor;
                    }
                }
            }
        }
        
        // Check for common retailer names
        String[] commonRetailers = {
            "Amazon", "Walmart", "Target", "Best Buy", "Costco", "Netflix", "Spotify",
            "Uber", "Lyft", "DoorDash", "Grubhub", "Instacart", "Apple", "Google",
            "Microsoft", "Steam", "PlayStation", "Xbox", "Adobe", "Zoom", "Slack"
        };
        
        for (String retailer : commonRetailers) {
            if (content.contains(retailer)) {
                return retailer;
            }
        }
        
        // Default if no vendor found
        return "Unknown Vendor";
    }
    
    private String extractBriefDescription(String content) {
        // Look for common description indicators
        String[] descKeywords = {"description:", "item:", "product:", "service:", "regarding:", "for:"};
        
        for (String keyword : descKeywords) {
            int index = content.toLowerCase().indexOf(keyword);
            if (index >= 0) {
                // Extract text after the keyword until a newline or period
                int start = index + keyword.length();
                int end = content.indexOf('\n', start);
                if (end == -1) end = content.indexOf('.', start);
                if (end == -1) end = start + 50; // Limit to 50 chars if no terminator found
                if (end > start) {
                    String desc = content.substring(start, end).trim();
                    if (!desc.isEmpty()) {
                        return desc;
                    }
                }
            }
        }
        
        // If no specific description found, extract from subject line or first few words
        int subjectIndex = content.toLowerCase().indexOf("subject:");
        if (subjectIndex >= 0) {
            int start = subjectIndex + 8;
            int end = content.indexOf('\n', start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        }
        
        // Default description
        return "Transaction from email";
    }
    
    private boolean isRecurring(String content) {
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("subscription") || lowerContent.contains("recurring") ||
               lowerContent.contains("monthly") || lowerContent.contains("yearly") ||
               lowerContent.contains("weekly") || lowerContent.contains("quarterly") ||
               lowerContent.contains("annual") || lowerContent.contains("membership");
    }
    
    private String extractRecurrencePattern(String content) {
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("monthly")) return "MONTHLY";
        if (lowerContent.contains("weekly")) return "WEEKLY";
        if (lowerContent.contains("yearly") || lowerContent.contains("annual")) return "YEARLY";
        if (lowerContent.contains("quarterly")) return "QUARTERLY";
        if (lowerContent.contains("daily")) return "DAILY";
        
        // Default to monthly if recurring but pattern not clear
        return "MONTHLY";
    }
}
