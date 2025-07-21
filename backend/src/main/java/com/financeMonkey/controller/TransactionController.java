package com.financeMonkey.controller;

import com.financeMonkey.dto.TransactionDto;
import com.financeMonkey.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionDto>> getUserTransactions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        String token = authHeader.substring(7);
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(transactionService.getUserTransactionsByDateRange(token, startDate, endDate, pageable));
        } else {
            return ResponseEntity.ok(transactionService.getUserTransactions(token, pageable));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID transactionId) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(transactionService.getTransactionById(token, transactionId));
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID transactionId,
            @RequestBody TransactionDto transactionDto) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(transactionService.updateTransaction(token, transactionId, transactionDto));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID transactionId) {
        
        String token = authHeader.substring(7);
        transactionService.deleteTransaction(token, transactionId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TransactionDto transactionDto) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(transactionService.createTransaction(token, transactionDto));
    }
}
