package com.financeMonkey.dto;

import com.financeMonkey.model.EmailAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccountDto {
    private UUID id;
    private String emailAddress;
    private LocalDateTime lastSyncDate;
    private String syncStatus;
    
    public static EmailAccountDto fromEntity(EmailAccount emailAccount) {
        return EmailAccountDto.builder()
                .id(emailAccount.getId())
                .emailAddress(emailAccount.getEmailAddress())
                .lastSyncDate(emailAccount.getLastSyncDate())
                .syncStatus(emailAccount.getSyncStatus().name())
                .build();
    }
}
