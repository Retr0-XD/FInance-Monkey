package com.financeMonkey.service;

import com.financeMonkey.dto.TransactionDto;
import com.financeMonkey.model.Category;
import com.financeMonkey.model.Transaction;
import com.financeMonkey.model.User;
import com.financeMonkey.repository.CategoryRepository;
import com.financeMonkey.repository.TransactionRepository;
import com.financeMonkey.repository.UserRepository;
import com.financeMonkey.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getUserTransactions(String token, Pageable pageable) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Page<Transaction> transactions = transactionRepository.findByUser(user, pageable);
        return transactions.map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getUserTransactionsByDateRange(
            String token, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Page<Transaction> transactions = transactionRepository
                .findByUserAndTransactionDateBetween(user, startDate, endDate, pageable);
        return transactions.map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(String token, UUID transactionId) {
        UUID userId = jwtTokenProvider.getUserId(token);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
        
        // Verify ownership
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to access this transaction");
        }
        
        return TransactionDto.fromEntity(transaction);
    }

    @Transactional
    public TransactionDto updateTransaction(String token, UUID transactionId, TransactionDto transactionDto) {
        UUID userId = jwtTokenProvider.getUserId(token);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
        
        // Verify ownership
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this transaction");
        }
        
        transaction.setTransactionDate(transactionDto.getTransactionDate());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setCurrency(transactionDto.getCurrency());
        transaction.setVendor(transactionDto.getVendor());
        transaction.setDescription(transactionDto.getDescription());
        transaction.setRecurring(transactionDto.isRecurring());
        transaction.setRecurrencePattern(transactionDto.getRecurrencePattern());
        
        if (transactionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(transactionDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + transactionDto.getCategoryId()));
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionDto.fromEntity(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(String token, UUID transactionId) {
        UUID userId = jwtTokenProvider.getUserId(token);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + transactionId));
        
        // Verify ownership
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this transaction");
        }
        
        transactionRepository.deleteById(transactionId);
    }
    
    @Transactional
    public TransactionDto createTransaction(String token, TransactionDto transactionDto) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        Transaction transaction = Transaction.builder()
            .user(user)
            .transactionDate(transactionDto.getTransactionDate() != null ? transactionDto.getTransactionDate() : LocalDateTime.now())
            .amount(transactionDto.getAmount() != null ? transactionDto.getAmount() : BigDecimal.ZERO)
            .currency(transactionDto.getCurrency() != null ? transactionDto.getCurrency() : "USD")
            .vendor(transactionDto.getVendor() != null ? transactionDto.getVendor() : "Unknown Vendor")
            .description(transactionDto.getDescription())
            .recurring(transactionDto.isRecurring())
            .recurrencePattern(transactionDto.getRecurrencePattern())
            .status(Transaction.TransactionStatus.PROCESSED)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        if (transactionDto.getCategoryId() != null) {
            try {
                Category category = categoryRepository.findById(transactionDto.getCategoryId())
                        .orElse(null);
                transaction.setCategory(category);
            } catch (Exception e) {
                // If category ID is invalid, just leave it null
            }
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionDto.fromEntity(savedTransaction);
    }
}
