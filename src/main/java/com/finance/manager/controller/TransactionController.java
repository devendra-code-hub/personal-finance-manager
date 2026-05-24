package com.finance.manager.controller;

import com.finance.manager.dto.request.CreateTransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.service.AuthService;
import com.finance.manager.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request, user));
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            // Also support filtering by category name (used by test script)
            @RequestParam(required = false) String category,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(transactionService.getTransactions(user, startDate, endDate, categoryId, category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest request,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(transactionService.updateTransaction(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.ok(transactionService.deleteTransaction(id, user));
    }
}