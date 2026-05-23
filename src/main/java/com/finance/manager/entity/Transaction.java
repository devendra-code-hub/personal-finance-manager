package com.finance.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a financial transaction (income or expense).
 * BigDecimal for amount — why not Double? Double has floating point precision issues.
 * For money, always use BigDecimal (e.g., 0.1 + 0.2 != 0.3 in double arithmetic).
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Stored as LocalDate — no time zone issues, assignment uses YYYY-MM-DD format
    @Column(nullable = false)
    private LocalDate date;

    // Category linked — type (INCOME/EXPENSE) is derived from category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String description;

    // Which user owns this transaction — enforces data isolation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}