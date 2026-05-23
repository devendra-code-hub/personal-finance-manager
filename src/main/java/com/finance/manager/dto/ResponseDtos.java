package com.finance.manager.dto.response;

import com.finance.manager.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * All response DTOs in one file for brevity.
 * In a large project you'd split these into separate files.
 */
public class ResponseDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RegisterResponse {
        private String message;
        private Long userId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private LocalDate date;
        private String category;
        private String description;
        private TransactionType type;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TransactionListResponse {
        private List<TransactionResponse> transactions;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CategoryResponse {
        private String name;
        private TransactionType type;
        private boolean isCustom;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CategoryListResponse {
        private List<CategoryResponse> categories;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GoalResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private LocalDate startDate;
        private BigDecimal currentProgress;
        private Double progressPercentage;
        private BigDecimal remainingAmount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GoalListResponse {
        private List<GoalResponse> goals;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyReportResponse {
        private int month;
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class YearlyReportResponse {
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
    }
}