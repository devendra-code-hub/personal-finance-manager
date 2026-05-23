package com.finance.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a transaction category (e.g., Salary, Food, Rent).
 * Two types: DEFAULT (system-provided, not deletable) and CUSTOM (user-created).
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // INCOME or EXPENSE

    // null for default categories, set for custom categories
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // true = user-created, false = system default (Salary, Food, Rent, etc.)
    @Column(nullable = false)
    private boolean isCustom;

    // Soft delete flag: we can't physically delete categories referenced by transactions
    // Instead we mark them deleted — this also prevents them appearing in listings
    @Column(nullable = false)
    private boolean deleted = false;
}