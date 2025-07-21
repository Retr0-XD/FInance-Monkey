package com.financeMonkey.service;

import com.financeMonkey.model.*;
import com.financeMonkey.repository.EmailAccountRepository;
import com.financeMonkey.repository.ProcessedEmailRepository;
import com.financeMonkey.repository.TransactionRepository;
import com.financeMonkey.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessingService {
    
    private final EmailAccountRepository emailAccountRepository;
    private final ProcessedEmailRepository processedEmailRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final GeminiAIService geminiAIService;
    
    @Value("${email.processing.batch-size}")
    private int batchSize;
    
    @Value("${email.processing.max-retries}")
    private int maxRetries;

    // Scheduled job to process emails periodically
    @Scheduled(cron = "${email.processing.schedule}")
    @Transactional
    public void processNewEmails() {
        log.info("Starting scheduled email processing");
        
        List<EmailAccount> connectedAccounts = emailAccountRepository
                .findByUserAndSyncStatus(null, EmailAccount.SyncStatus.CONNECTED);
        
        for (EmailAccount account : connectedAccounts) {
            try {
                processEmailsForAccount(account);
            } catch (Exception e) {
                log.error("Error processing emails for account: {}", account.getEmailAddress(), e);
                // Update account status to reflect failure
                account.setSyncStatus(EmailAccount.SyncStatus.FAILED);
                emailAccountRepository.save(account);
            }
        }
        
        log.info("Completed scheduled email processing");
    }
    
    @Transactional
    public void processEmailsForAccount(EmailAccount account) {
        log.info("Processing emails for account: {}", account.getEmailAddress());
        
        account.setSyncStatus(EmailAccount.SyncStatus.SYNCING);
        emailAccountRepository.save(account);
        
        try {
            // Fetch emails using Gmail API
            List<EmailData> emails = fetchEmailsFromGmail(account);
            log.info("Fetched {} emails from account: {}", emails.size(), account.getEmailAddress());
            
            // Process each email
            for (EmailData emailData : emails) {
                processEmail(account, emailData.getMessageId(), emailData.getSubject(), emailData.getContent());
            }
            
            // Update account status after successful sync
            account.setLastSyncDate(LocalDateTime.now());
            account.setSyncStatus(EmailAccount.SyncStatus.CONNECTED);
            emailAccountRepository.save(account);
            
            log.info("Successfully processed emails for account: {}", account.getEmailAddress());
        } catch (Exception e) {
            log.error("Failed to process emails for account: {}", account.getEmailAddress(), e);
            account.setSyncStatus(EmailAccount.SyncStatus.FAILED);
            emailAccountRepository.save(account);
            throw e;
        }
    }
    
    /**
     * Fetches emails from Gmail using the Gmail API.
     */
    private List<EmailData> fetchEmailsFromGmail(EmailAccount account) {
        List<EmailData> emails = new ArrayList<>();
        
        try {
            // Set up credentials
            GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets("client_id", "client_secret") // These would be configured in application properties
                .build();
            
            credential.setAccessToken(account.getAccessToken());
            credential.setRefreshToken(account.getRefreshToken());
            
            // Initialize Gmail service
            Gmail service = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Finance Monkey")
                .build();
            
            // Get messages from last sync date or default to last 30 days
            LocalDateTime startDate = account.getLastSyncDate();
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            
            // Format date for Gmail query
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String afterDate = startDate.format(formatter);
            
            // Query for financial emails only
            String query = "after:" + afterDate + " (category:primary OR category:promotions OR category:updates) " +
                           "(subject:payment OR subject:receipt OR subject:transaction OR subject:invoice OR " +
                           "subject:order OR subject:purchase)";
            
            // List messages matching query
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(batchSize)
                .setQ(query)
                .execute();
                
            if (listResponse.getMessages() != null) {
                // For each message ID, get the full message
                for (Message message : listResponse.getMessages()) {
                    // Check if we've already processed this email
                    String messageId = message.getId();
                    if (processedEmailRepository.existsByEmailAccountAndEmailMessageId(account, messageId)) {
                        continue;
                    }
                    
                    try {
                        // Get the full message
                        Message fullMessage = service.users().messages().get("me", messageId).execute();
                        
                        // Extract subject
                        String subject = "";
                        for (MessagePartHeader header : fullMessage.getPayload().getHeaders()) {
                            if (header.getName().equals("Subject")) {
                                subject = header.getValue();
                                break;
                            }
                        }
                        
                        // Extract content
                        String content = extractEmailContent(fullMessage);
                        
                        // Add to our list
                        emails.add(new EmailData(messageId, subject, content));
                    } catch (Exception e) {
                        log.warn("Error fetching message {}: {}", messageId, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error accessing Gmail API", e);
            throw new RuntimeException("Failed to fetch emails from Gmail", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching emails", e);
            throw new RuntimeException("Failed to fetch emails", e);
        }
        
        return emails;
    }
    
    /**
     * Extracts email content from a Gmail message.
     */
    private String extractEmailContent(Message message) {
        StringBuilder contentBuilder = new StringBuilder();
        
        try {
            if (message.getPayload() != null) {
                extractContent(message.getPayload(), contentBuilder);
            }
        } catch (Exception e) {
            log.warn("Error extracting content from message: {}", e.getMessage());
        }
        
        return contentBuilder.toString();
    }
    
    /**
     * Recursively extracts content from message parts.
     */
    private void extractContent(MessagePart part, StringBuilder contentBuilder) {
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                extractContent(subPart, contentBuilder);
            }
        } else if (part.getBody() != null && part.getBody().getData() != null) {
            String mimeType = part.getMimeType();
            if (mimeType.equals("text/plain") || mimeType.equals("text/html")) {
                String data = part.getBody().getData();
                byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
                String text = new String(decodedBytes, StandardCharsets.UTF_8);
                
                // For HTML content, try to extract just the text
                if (mimeType.equals("text/html")) {
                    text = extractTextFromHtml(text);
                }
                
                contentBuilder.append(text).append("\n");
            }
        }
    }
    
    /**
     * Simple HTML to text conversion.
     */
    private String extractTextFromHtml(String html) {
        return html.replaceAll("<[^>]*>", " ")
                  .replaceAll("\\s+", " ")
                  .trim();
    }
    
    /**
     * Simple class to hold email data.
     */
    private static class EmailData {
        private final String messageId;
        private final String subject;
        private final String content;
        
        public EmailData(String messageId, String subject, String content) {
            this.messageId = messageId;
            this.subject = subject;
            this.content = content;
        }
        
        public String getMessageId() {
            return messageId;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public String getContent() {
            return content;
        }
    }
    
    private final CategoryRepository categoryRepository;
    
    private void processEmail(EmailAccount account, String messageId, String subject, String content) {
        // Check if we've already processed this email
        if (processedEmailRepository.existsByEmailAccountAndEmailMessageId(account, messageId)) {
            return;
        }
        
        ProcessedEmail processedEmail = new ProcessedEmail();
        processedEmail.setEmailAccount(account);
        processedEmail.setEmailMessageId(messageId);
        processedEmail.setSubject(subject);
        
        try {
            // Use Gemini AI to extract transaction information
            TransactionInfo transactionInfo = geminiAIService.extractTransactionInfo(content);
            
            if (transactionInfo != null) {
                // Create and save transaction
                Transaction transaction = new Transaction();
                transaction.setUser(account.getUser());
                transaction.setEmailAccount(account);
                transaction.setTransactionDate(transactionInfo.getTransactionDate());
                transaction.setAmount(transactionInfo.getAmount());
                transaction.setCurrency(transactionInfo.getCurrency());
                transaction.setVendor(transactionInfo.getVendor());
                transaction.setDescription(transactionInfo.getDescription());
                transaction.setRecurring(transactionInfo.isRecurring());
                transaction.setRecurrencePattern(transactionInfo.getRecurrencePattern());
                transaction.setStatus(Transaction.TransactionStatus.PROCESSED);
                
                // Determine category based on transaction data
                Category category = determineCategory(transaction);
                if (category != null) {
                    transaction.setCategory(category);
                }
                
                transactionRepository.save(transaction);
                
                log.info("Saved new transaction: amount={}, vendor={}, category={}", 
                         transaction.getAmount(),
                         transaction.getVendor(),
                         category != null ? category.getName() : "uncategorized");
                
                processedEmail.setProcessingStatus(ProcessedEmail.ProcessingStatus.SUCCESS);
            } else {
                // No transaction found in this email
                processedEmail.setProcessingStatus(ProcessedEmail.ProcessingStatus.IGNORED);
            }
        } catch (Exception e) {
            log.error("Error processing email: {}", messageId, e);
            processedEmail.setProcessingStatus(ProcessedEmail.ProcessingStatus.FAILED);
            processedEmail.setErrorMessage(e.getMessage());
        }
        
        processedEmailRepository.save(processedEmail);
    }
    
    /**
     * Determines the appropriate category for a transaction based on vendor and description.
     */
    private Category determineCategory(Transaction transaction) {
        String vendor = transaction.getVendor().toLowerCase();
        String description = transaction.getDescription() != null ? 
                            transaction.getDescription().toLowerCase() : "";
        
        // List of category mappings - keyword patterns to UUID of predefined categories
        List<CategoryMapping> categoryMappings = getCategoryMappings();
        
        for (CategoryMapping mapping : categoryMappings) {
            for (Pattern pattern : mapping.getPatterns()) {
                if (pattern.matcher(vendor).find() || 
                    (description != null && pattern.matcher(description).find())) {
                    try {
                        return categoryRepository.findById(mapping.getCategoryId()).orElse(null);
                    } catch (Exception e) {
                        log.warn("Failed to find category with ID: {}", mapping.getCategoryId(), e);
                    }
                }
            }
        }
        
        // Return null if no matching category found
        return null;
    }
    
    /**
     * Returns a list of category mappings with regex patterns.
     */
    private List<CategoryMapping> getCategoryMappings() {
        List<CategoryMapping> mappings = new ArrayList<>();
        
        // Bills category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",  // Bills category ID
            List.of(
                Pattern.compile("electricity|power|utility|gas|water|sewage|garbage|waste|internet|phone|cable|mortgage|rent|insurance|bill"),
                Pattern.compile("verizon|at&t|comcast|xfinity|sprint|t-mobile|spectrum")
            )
        ));
        
        // Food & Dining category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12",  // Food & Dining category ID
            List.of(
                Pattern.compile("restaurant|food|grocery|meal|dinner|lunch|breakfast|cafe|coffee|doordash|grubhub|ubereats|instacart"),
                Pattern.compile("starbucks|mcdonald|chipotle|subway|taco|burger|pizza|deli|bakery")
            )
        ));
        
        // Shopping category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13",  // Shopping category ID
            List.of(
                Pattern.compile("amazon|walmart|target|bestbuy|costco|ikea|clothing|shoes|electronics|purchase|store|shop|mall"),
                Pattern.compile("ebay|etsy|wayfair|home depot|lowes|macys|nordstrom|purchase")
            )
        ));
        
        // Entertainment category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14",  // Entertainment category ID
            List.of(
                Pattern.compile("movie|theatre|theater|netflix|hulu|disney|spotify|pandora|apple music|concert|ticket|game"),
                Pattern.compile("cinema|amc|regal|fandango|entertainment|hbo|showtime|playstation|xbox|steam")
            )
        ));
        
        // Transportation category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15",  // Transportation category ID
            List.of(
                Pattern.compile("uber|lyft|taxi|cab|train|subway|metro|bus|transport|fare|ticket|gas|fuel|parking"),
                Pattern.compile("amtrak|transit|airline|flight|travel|car service|toll")
            )
        ));
        
        // Travel category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16",  // Travel category ID
            List.of(
                Pattern.compile("hotel|airbnb|vrbo|motel|resort|booking|expedia|kayak|airline|flight|cruise|vacation"),
                Pattern.compile("travelocity|orbitz|priceline|tripadvisor|delta|united|american airlines|southwest")
            )
        ));
        
        // Health category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17",  // Health category ID
            List.of(
                Pattern.compile("doctor|hospital|clinic|pharmacy|medicine|medical|dental|vision|healthcare|health|cvs|walgreens"),
                Pattern.compile("therapy|prescription|rite aid|urgent care|laboratory|lab")
            )
        ));
        
        // Subscriptions category
        mappings.add(new CategoryMapping(
            "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18",  // Subscriptions category ID
            List.of(
                Pattern.compile("subscription|membership|recurring|monthly|plan|service|netflix|hulu|disney|spotify"),
                Pattern.compile("apple|google|microsoft|adobe|zoom|amazon prime|youtube|hbo|audible")
            )
        ));
        
        return mappings;
    }
    
    /**
     * Helper class to store category mapping information.
     */
    private static class CategoryMapping {
        private final UUID categoryId;
        private final List<Pattern> patterns;
        
        public CategoryMapping(String categoryId, List<Pattern> patterns) {
            this.categoryId = UUID.fromString(categoryId);
            this.patterns = patterns;
        }
        
        public UUID getCategoryId() {
            return categoryId;
        }
        
        public List<Pattern> getPatterns() {
            return patterns;
        }
    }
    
    // Class to hold transaction information extracted by AI
    public static class TransactionInfo {
        private LocalDateTime transactionDate;
        private java.math.BigDecimal amount;
        private String currency;
        private String vendor;
        private String description;
        private boolean recurring;
        private String recurrencePattern;
        
        // Getters and setters
        public LocalDateTime getTransactionDate() {
            return transactionDate;
        }
        
        public void setTransactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
        }
        
        public java.math.BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(java.math.BigDecimal amount) {
            this.amount = amount;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getVendor() {
            return vendor;
        }
        
        public void setVendor(String vendor) {
            this.vendor = vendor;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public boolean isRecurring() {
            return recurring;
        }
        
        public void setRecurring(boolean recurring) {
            this.recurring = recurring;
        }
        
        public String getRecurrencePattern() {
            return recurrencePattern;
        }
        
        public void setRecurrencePattern(String recurrencePattern) {
            this.recurrencePattern = recurrencePattern;
        }
    }
}
