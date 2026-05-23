package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions for a user, sorted newest first (for GET /api/transactions)
    List<Transaction> findByUserOrderByDateDesc(User user);

    // Filtered query for date range + category (used in GET /api/transactions?startDate=...&categoryId=...)
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "ORDER BY t.date DESC")
    List<Transaction> findWithFilters(
        @Param("user") User user,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("category") Category category
    );

    // Find user's specific transaction — used before update/delete to verify ownership
    Optional<Transaction> findByIdAndUser(Long id, User user);

    // For savings goal progress: sum of income - expenses since goal start date
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = :type AND t.date >= :startDate")
    BigDecimal sumByUserAndTypeAndDateAfter(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("startDate") LocalDate startDate
    );

    // For monthly report: sum by category type in a specific month/year
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = :type " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "GROUP BY t.category.name")
    List<Object[]> sumByCategoryForMonth(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("year") int year,
        @Param("month") int month
    );

    // For yearly report
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = :type " +
           "AND YEAR(t.date) = :year " +
           "GROUP BY t.category.name")
    List<Object[]> sumByCategoryForYear(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("year") int year
    );

    // Check if any transactions reference a category (needed before allowing category deletion)
    boolean existsByCategory(Category category);
}