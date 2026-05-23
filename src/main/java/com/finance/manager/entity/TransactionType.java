package com.finance.manager.entity;

/**
 * Enum for transaction/category type.
 * Stored as STRING in DB (not ordinal) so DB data is human-readable
 * and safe if enum order changes.
 */
public enum TransactionType {
    INCOME,
    EXPENSE
}