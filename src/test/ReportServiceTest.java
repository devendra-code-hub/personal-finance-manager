package com.finance.manager.service;

import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.*;
import com.finance.manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private ReportService reportService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
    }

    @Test
    void getMonthlyReport_Success() {
        when(transactionRepository.sumByCategoryForMonth(testUser, TransactionType.INCOME, 2024, 1))
            .thenReturn(List.of(new Object[]{"Salary", new BigDecimal("3000.00")}));
        when(transactionRepository.sumByCategoryForMonth(testUser, TransactionType.EXPENSE, 2024, 1))
            .thenReturn(List.of(
                new Object[]{"Food", new BigDecimal("400.00")},
                new Object[]{"Rent", new BigDecimal("1200.00")}
            ));

        MonthlyReportResponse report = reportService.getMonthlyReport(testUser, 2024, 1);

        assertEquals(1, report.getMonth());
        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        // Net savings = 3000 - (400 + 1200) = 1400
        assertEquals(new BigDecimal("1400.00"), report.getNetSavings());
    }

    @Test
    void getMonthlyReport_NoTransactions_ZeroValues() {
        when(transactionRepository.sumByCategoryForMonth(any(), any(), anyInt(), anyInt()))
            .thenReturn(List.of());

        MonthlyReportResponse report = reportService.getMonthlyReport(testUser, 2024, 6);

        assertTrue(report.getTotalIncome().isEmpty());
        assertTrue(report.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getNetSavings());
    }

    @Test
    void getYearlyReport_Success() {
        when(transactionRepository.sumByCategoryForYear(testUser, TransactionType.INCOME, 2024))
            .thenReturn(List.of(new Object[]{"Salary", new BigDecimal("36000.00")}));
        when(transactionRepository.sumByCategoryForYear(testUser, TransactionType.EXPENSE, 2024))
            .thenReturn(List.of(new Object[]{"Rent", new BigDecimal("14400.00")}));

        YearlyReportResponse report = reportService.getYearlyReport(testUser, 2024);

        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("36000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("21600.00"), report.getNetSavings()); // 36000 - 14400
    }
}