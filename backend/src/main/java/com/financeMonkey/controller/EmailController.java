package com.financeMonkey.controller;

import com.financeMonkey.dto.EmailAccountDto;
import com.financeMonkey.service.EmailAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailAccountService emailAccountService;

    @GetMapping("/accounts")
    public ResponseEntity<List<EmailAccountDto>> getEmailAccounts(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return ResponseEntity.ok(emailAccountService.getEmailAccountsByUser(token));
    }

    @PostMapping("/connect")
    public ResponseEntity<EmailAccountDto> connectEmailAccount(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> connectRequest) {
        
        String token = authHeader.substring(7);
        String emailAddress = connectRequest.get("emailAddress");
        String accessToken = connectRequest.get("accessToken");
        String refreshToken = connectRequest.get("refreshToken");
        
        return new ResponseEntity<>(
                emailAccountService.connectEmailAccount(token, emailAddress, accessToken, refreshToken),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/accounts/{emailAccountId}")
    public ResponseEntity<Void> disconnectEmailAccount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID emailAccountId) {
        
        String token = authHeader.substring(7);
        emailAccountService.disconnectEmailAccount(token, emailAccountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts/{emailAccountId}")
    public ResponseEntity<EmailAccountDto> getEmailAccountById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID emailAccountId) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(emailAccountService.getEmailAccountById(token, emailAccountId));
    }

    // This endpoint will trigger a manual sync of emails
    @PostMapping("/sync")
    public ResponseEntity<Void> triggerEmailSync(
            @RequestHeader("Authorization") String authHeader) {
        // We'll implement this in the email processing service
        return ResponseEntity.accepted().build();
    }
}
