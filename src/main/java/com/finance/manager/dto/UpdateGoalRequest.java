package com.finance.manager.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateGoalRequest {

    @Positive(message = "Target amount must be positive")
    private BigDecimal targetAmount;

    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
}