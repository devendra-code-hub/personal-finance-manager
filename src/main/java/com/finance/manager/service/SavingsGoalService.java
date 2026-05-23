package com.finance.manager.service;

import com.finance.manager.dto.request.CreateGoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.entity.User;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    public GoalResponse createGoal(CreateGoalRequest request, User user) {
        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        // Default startDate to today if not provided
        goal.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        goal.setUser(user);

        SavingsGoal saved = goalRepository.save(goal);
        return toResponse(saved, user);
    }

    public GoalListResponse getAllGoals(User user) {
        List<GoalResponse> goals = goalRepository.findByUser(user)
                .stream()
                .map(g -> toResponse(g, user))
                .collect(Collectors.toList());
        return new GoalListResponse(goals);
    }

    public GoalResponse getGoal(Long id, User user) {
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        return toResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, UpdateGoalRequest request, User user) {
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = goalRepository.save(goal);
        return toResponse(updated, user);
    }

    public MessageResponse deleteGoal(Long id, User user) {
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goalRepository.delete(goal);
        return new MessageResponse("Goal deleted successfully");
    }

    /**
     * Calculate progress: (Total INCOME - Total EXPENSES) since goal's start date.
     * This is dynamic — recalculated from transactions each time.
     * Deleted transactions don't appear here because they're physically deleted.
     */
    private GoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateAfter(
                user, TransactionType.INCOME, goal.getStartDate());
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateAfter(
                user, TransactionType.EXPENSE, goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO; // Progress can't be negative
        }

        BigDecimal targetAmount = goal.getTargetAmount();

        double progressPercentage = 0.0;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                    .divide(targetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return new GoalResponse(
            goal.getId(),
            goal.getGoalName(),
            targetAmount,
            goal.getTargetDate(),
            goal.getStartDate(),
            currentProgress,
            progressPercentage,
            remainingAmount
        );
    }
}