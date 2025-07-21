package com.financeMonkey.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_emails")
public class ProcessedEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_account_id", nullable = false)
    private EmailAccount emailAccount;
    
    @Column(nullable = false)
    private String emailMessageId;
    
    private String subject;
    
    @Column(nullable = false)
    private LocalDateTime processedDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        processedDate = LocalDateTime.now();
        if (processingStatus == null) {
            processingStatus = ProcessingStatus.SUCCESS;
        }
    }
    
    public enum ProcessingStatus {
        SUCCESS, FAILED, IGNORED, RETRY_LATER
    }
}
