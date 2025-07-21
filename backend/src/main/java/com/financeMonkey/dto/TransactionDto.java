package com.financeMonkey.dto;

import com.financeMonkey.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private UUID id;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String currency;
    private String vendor;
    private UUID categoryId;
    private String categoryName;
    private String description;
    private boolean recurring;
    private String recurrencePattern;
    private String status;
    
    public static TransactionDto fromEntity(Transaction transaction) {
        TransactionDto dto = TransactionDto.builder()
                .id(transaction.getId())
                .transactionDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .vendor(transaction.getVendor())
                .description(transaction.getDescription())
                .recurring(transaction.isRecurring())
                .recurrencePattern(transaction.getRecurrencePattern())
                .status(transaction.getStatus().name())
                .build();
        
        if (transaction.getCategory() != null) {
            dto.setCategoryId(transaction.getCategory().getId());
            dto.setCategoryName(transaction.getCategory().getName());
        }
        
        return dto;
    }
}
