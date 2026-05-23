package com.finance.manager.controller;

import com.finance.manager.dto.request.CreateGoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.service.AuthService;
import com.finance.manager.service.SavingsGoalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService goalService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody CreateGoalRequest request,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(request, user));
    }

    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(goalService.getAllGoals(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(goalService.getGoal(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalRequest request,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(goalService.updateGoal(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(goalService.deleteGoal(id, user));
    }
}