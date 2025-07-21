package com.financeMonkey.controller;

import com.financeMonkey.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/transactions/stats")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTransactionStats(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(analyticsService.getTransactionStats(token));
    }

    @GetMapping("/spending")
    public ResponseEntity<Map<String, Object>> getSpendingSummary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(analyticsService.getSpendingSummary(token, startDate, endDate));
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<Map<String, Object>> getMonthlyTrends(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "12") int months) {
        
        String token = authHeader.substring(7);
        return ResponseEntity.ok(analyticsService.getMonthlyTrends(token, months));
    }
}
