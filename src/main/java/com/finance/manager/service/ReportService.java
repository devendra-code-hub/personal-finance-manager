package com.finance.manager.service;

import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        Map<String, BigDecimal> income = buildCategoryMap(
            transactionRepository.sumByCategoryForMonth(user, TransactionType.INCOME, year, month)
        );
        Map<String, BigDecimal> expenses = buildCategoryMap(
            transactionRepository.sumByCategoryForMonth(user, TransactionType.EXPENSE, year, month)
        );

        BigDecimal totalIncome = income.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return new MonthlyReportResponse(month, year, income, expenses, netSavings);
    }

    public YearlyReportResponse getYearlyReport(User user, int year) {
        Map<String, BigDecimal> income = buildCategoryMap(
            transactionRepository.sumByCategoryForYear(user, TransactionType.INCOME, year)
        );
        Map<String, BigDecimal> expenses = buildCategoryMap(
            transactionRepository.sumByCategoryForYear(user, TransactionType.EXPENSE, year)
        );

        BigDecimal totalIncome = income.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return new YearlyReportResponse(year, income, expenses, netSavings);
    }

    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            map.put(categoryName, amount);
        }
        return map;
    }
}