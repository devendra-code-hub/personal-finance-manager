package com.finance.manager.service;

import com.finance.manager.dto.request.CreateTransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionResponse createTransaction(CreateTransactionRequest request, User user) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        Category category = categoryRepository.findByNameAndUser(request.getCategory(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategory()));

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setUser(user);

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public TransactionListResponse getTransactions(User user, LocalDate startDate, LocalDate endDate,
                                                    Long categoryId, String categoryName) {
        Category filterCategory = null;

        // Support filtering by categoryId
        if (categoryId != null) {
            filterCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }
        // Support filtering by category name
        else if (categoryName != null && !categoryName.isEmpty()) {
            filterCategory = categoryRepository.findByNameAndUser(categoryName, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName));
        }

        List<TransactionResponse> transactions = transactionRepository
                .findWithFilters(user, startDate, endDate, filterCategory)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new TransactionListResponse(transactions);
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findByNameAndUser(request.getCategory(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategory()));
            transaction.setCategory(category);
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        // date is intentionally NOT updatable

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    public MessageResponse deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
        return new MessageResponse("Transaction deleted successfully");
    }

    public TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
            t.getId(), t.getAmount(), t.getDate(),
            t.getCategory().getName(), t.getDescription(),
            t.getCategory().getType()
        );
    }
}