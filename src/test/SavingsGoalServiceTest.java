package com.finance.manager.service;

import com.finance.manager.dto.request.CreateGoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository goalRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private SavingsGoalService goalService;

    private User testUser;
    private SavingsGoal testGoal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testGoal = new SavingsGoal();
        testGoal.setId(1L);
        testGoal.setGoalName("Emergency Fund");
        testGoal.setTargetAmount(new BigDecimal("5000.00"));
        testGoal.setTargetDate(LocalDate.now().plusMonths(6));
        testGoal.setStartDate(LocalDate.now().minusMonths(1));
        testGoal.setUser(testUser);
    }

    @Test
    void createGoal_DefaultStartDate() {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setGoalName("Vacation");
        request.setTargetAmount(new BigDecimal("2000.00"));
        request.setTargetDate(LocalDate.now().plusYears(1));
        // No startDate provided — should default to today

        when(goalRepository.save(any())).thenReturn(testGoal);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), eq(TransactionType.INCOME), any()))
            .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), eq(TransactionType.EXPENSE), any()))
            .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.createGoal(request, testUser);

        assertNotNull(response);
    }

    @Test
    void getGoal_CalculatesProgress() {
        when(goalRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testGoal));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(eq(testUser), eq(TransactionType.INCOME), any()))
            .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(eq(testUser), eq(TransactionType.EXPENSE), any()))
            .thenReturn(new BigDecimal("500.00"));

        GoalResponse response = goalService.getGoal(1L, testUser);

        // Progress = 2000 - 500 = 1500
        assertEquals(new BigDecimal("1500.00"), response.getCurrentProgress());
        assertEquals(30.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("3500.00"), response.getRemainingAmount());
    }

    @Test
    void getGoal_NegativeProgress_ClampsToZero() {
        when(goalRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testGoal));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(eq(testUser), eq(TransactionType.INCOME), any()))
            .thenReturn(new BigDecimal("100.00"));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(eq(testUser), eq(TransactionType.EXPENSE), any()))
            .thenReturn(new BigDecimal("500.00")); // More expenses than income

        GoalResponse response = goalService.getGoal(1L, testUser);

        assertEquals(BigDecimal.ZERO, response.getCurrentProgress());
    }

    @Test
    void updateGoal_Success() {
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000.00"));

        when(goalRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any())).thenReturn(testGoal);
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), any(), any()))
            .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.updateGoal(1L, request, testUser);
        assertNotNull(response);
        verify(goalRepository).save(any());
    }

    @Test
    void deleteGoal_NotFound_ThrowsException() {
        when(goalRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> goalService.deleteGoal(99L, testUser));
    }

    @Test
    void getAllGoals_ReturnsAll() {
        when(goalRepository.findByUser(testUser)).thenReturn(List.of(testGoal));
        when(transactionRepository.sumByUserAndTypeAndDateAfter(any(), any(), any()))
            .thenReturn(BigDecimal.ZERO);

        GoalListResponse response = goalService.getAllGoals(testUser);
        assertEquals(1, response.getGoals().size());
    }
}