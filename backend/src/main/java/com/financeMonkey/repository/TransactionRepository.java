package com.financeMonkey.repository;

import com.financeMonkey.model.Category;
import com.financeMonkey.model.Transaction;
import com.financeMonkey.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Page<Transaction> findByUser(User user, Pageable pageable);
    Page<Transaction> findByUserAndTransactionDateBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Transaction> findByUserAndCategory(User user, Category category);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY t.category")
    List<Object[]> sumAmountByUserAndCategoryAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.recurring = true")
    Long countRecurringTransactionsByUser(@Param("user") User user);
    
    /**
     * Find all transactions by user ID
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId")
    List<Transaction> findByUserId(@Param("userId") String userId);
    
    /**
     * Find all distinct user IDs that have transactions
     */
    @Query("SELECT DISTINCT t.user.id FROM Transaction t")
    List<String> findDistinctUserIds();
}
