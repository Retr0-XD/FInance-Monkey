package com.financeMonkey.service;

import com.financeMonkey.dto.EmailAccountDto;
import com.financeMonkey.model.EmailAccount;
import com.financeMonkey.model.User;
import com.financeMonkey.repository.EmailAccountRepository;
import com.financeMonkey.repository.UserRepository;
import com.financeMonkey.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public List<EmailAccountDto> getEmailAccountsByUser(String token) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        return emailAccountRepository.findByUser(user).stream()
                .map(EmailAccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmailAccountDto connectEmailAccount(String token, String emailAddress, String accessToken, String refreshToken) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Check if email account already exists for this user
        return emailAccountRepository.findByUserAndEmailAddress(user, emailAddress)
                .map(existingAccount -> {
                    // Update the existing account with new tokens
                    existingAccount.setAccessToken(accessToken);
                    existingAccount.setRefreshToken(refreshToken);
                    existingAccount.setSyncStatus(EmailAccount.SyncStatus.CONNECTED);
                    EmailAccount updatedAccount = emailAccountRepository.save(existingAccount);
                    return EmailAccountDto.fromEntity(updatedAccount);
                })
                .orElseGet(() -> {
                    // Create a new account
                    EmailAccount emailAccount = EmailAccount.builder()
                            .user(user)
                            .emailAddress(emailAddress)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .syncStatus(EmailAccount.SyncStatus.CONNECTED)
                            .build();
                    
                    EmailAccount savedAccount = emailAccountRepository.save(emailAccount);
                    return EmailAccountDto.fromEntity(savedAccount);
                });
    }

    @Transactional
    public void disconnectEmailAccount(String token, UUID emailAccountId) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        EmailAccount emailAccount = emailAccountRepository.findById(emailAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Email account not found with id: " + emailAccountId));
        
        // Verify ownership
        if (!emailAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to disconnect this email account");
        }
        
        emailAccountRepository.deleteById(emailAccountId);
    }

    @Transactional(readOnly = true)
    public EmailAccountDto getEmailAccountById(String token, UUID emailAccountId) {
        UUID userId = jwtTokenProvider.getUserId(token);
        
        EmailAccount emailAccount = emailAccountRepository.findById(emailAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Email account not found with id: " + emailAccountId));
        
        // Verify ownership
        if (!emailAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to access this email account");
        }
        
        return EmailAccountDto.fromEntity(emailAccount);
    }
}
