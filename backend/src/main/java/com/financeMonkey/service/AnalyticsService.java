package com.financeMonkey.service;

import com.financeMonkey.dto.CategoryDto;
import com.financeMonkey.dto.TransactionDto;
import com.financeMonkey.model.User;
import com.financeMonkey.repository.TransactionRepository;
import com.financeMonkey.repository.UserRepository;
import com.financeMonkey.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public Map<String, Object> getSpendingSummary(String token, LocalDateTime startDate, LocalDateTime endDate) {
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        BigDecimal totalSpending = transactionRepository.sumAmountByUserAndDateRange(user, startDate, endDate);
        
        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }
        
        // Get spending by category
        List<Object[]> categorySpending = transactionRepository.sumAmountByUserAndCategoryAndDateRange(
                user, startDate, endDate);
        
        List<Map<String, Object>> spendingByCategory = new ArrayList<>();
        
        for (Object[] result : categorySpending) {
            Map<String, Object> categoryData = new HashMap<>();
            
            if (result[0] != null) {
                CategoryDto category = categoryService.getCategoryById(((UUID) result[0]));
                categoryData.put("categoryId", category.getId());
                categoryData.put("categoryName", category.getName());
            } else {
                categoryData.put("categoryId", null);
                categoryData.put("categoryName", "Uncategorized");
            }
            
            categoryData.put("amount", result[1]);
            spendingByCategory.add(categoryData);
        }
        
        // Count recurring transactions
        Long recurringCount = transactionRepository.countRecurringTransactionsByUser(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalSpending", totalSpending);
        result.put("spendingByCategory", spendingByCategory);
        result.put("recurringTransactionsCount", recurringCount);
        result.put("periodStart", startDate);
        result.put("periodEnd", endDate);
        
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyTrends(String token, int months) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);
        
        UUID userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        // For each month in the range
        for (int i = 0; i < months; i++) {
            LocalDateTime monthStart = startDate.plusMonths(i).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            LocalDateTime monthEnd;
            
            if (i == months - 1) {
                monthEnd = endDate;
            } else {
                monthEnd = startDate.plusMonths(i + 1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            }
            
            BigDecimal monthlySpending = transactionRepository.sumAmountByUserAndDateRange(
                    user, monthStart, monthEnd);
            
            if (monthlySpending == null) {
                monthlySpending = BigDecimal.ZERO;
            }
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthStart.getMonth().toString());
            monthData.put("year", monthStart.getYear());
            monthData.put("spending", monthlySpending);
            
            monthlyData.add(monthData);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("monthlyTrends", monthlyData);
        
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStats(String token) {
        // Get today, this week, and this month's spending
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        
        Map<String, Object> todayStats = getSpendingSummary(token, startOfDay, now);
        Map<String, Object> weekStats = getSpendingSummary(token, startOfWeek, now);
        Map<String, Object> monthStats = getSpendingSummary(token, startOfMonth, now);
        Map<String, Object> yearlyTrend = getMonthlyTrends(token, 12);
        
        Map<String, Object> result = new HashMap<>();
        result.put("today", todayStats);
        result.put("thisWeek", weekStats);
        result.put("thisMonth", monthStats);
        result.put("yearlyTrend", yearlyTrend);
        
        return result;
    }
}
