package com.finance.manager.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Update request — date is intentionally excluded.
 * The assignment says "Users can modify any transaction field except the date field."
 */
@Data
public class UpdateTransactionRequest {

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String category;

    private String description;
}