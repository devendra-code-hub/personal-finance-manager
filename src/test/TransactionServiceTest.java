package com.finance.manager.service;

import com.finance.manager.dto.request.CreateTransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.CategoryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private TransactionService transactionService;

    private User testUser;
    private Category salaryCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");

        salaryCategory = new Category();
        salaryCategory.setId(1L);
        salaryCategory.setName("Salary");
        salaryCategory.setType(TransactionType.INCOME);
        salaryCategory.setCustom(false);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("50000.00"));
        testTransaction.setDate(LocalDate.now().minusDays(1));
        testTransaction.setCategory(salaryCategory);
        testTransaction.setDescription("January Salary");
        testTransaction.setUser(testUser);
    }

    @Test
    void createTransaction_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("50000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");

        when(categoryRepository.findByNameAndUser("Salary", testUser))
            .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        TransactionResponse response = transactionService.createTransaction(request, testUser);

        assertEquals(new BigDecimal("50000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals(TransactionType.INCOME, response.getType());
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequest() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100"));
        request.setDate(LocalDate.now().plusDays(1)); // Future date
        request.setCategory("Salary");

        assertThrows(BadRequestException.class,
            () -> transactionService.createTransaction(request, testUser));
    }

    @Test
    void createTransaction_InvalidCategory_ThrowsNotFound() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(new BigDecimal("100"));
        request.setDate(LocalDate.now());
        request.setCategory("NonExistent");

        when(categoryRepository.findByNameAndUser("NonExistent", testUser))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> transactionService.createTransaction(request, testUser));
    }

    @Test
    void getTransactions_NoFilters_ReturnsAll() {
        when(transactionRepository.findWithFilters(testUser, null, null, null))
            .thenReturn(List.of(testTransaction));

        TransactionListResponse response = transactionService.getTransactions(testUser, null, null, null);

        assertEquals(1, response.getTransactions().size());
    }

    @Test
    void updateTransaction_Success() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("60000.00"));

        testTransaction.setAmount(new BigDecimal("60000.00"));

        when(transactionRepository.findByIdAndUser(1L, testUser))
            .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        TransactionResponse response = transactionService.updateTransaction(1L, request, testUser);

        assertEquals(new BigDecimal("60000.00"), response.getAmount());
    }

    @Test
    void updateTransaction_NotFound_ThrowsNotFound() {
        when(transactionRepository.findByIdAndUser(99L, testUser))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> transactionService.updateTransaction(99L, new UpdateTransactionRequest(), testUser));
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.findByIdAndUser(1L, testUser))
            .thenReturn(Optional.of(testTransaction));

        MessageResponse response = transactionService.deleteTransaction(1L, testUser);

        assertEquals("Transaction deleted successfully", response.getMessage());
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void deleteTransaction_NotOwned_ThrowsNotFound() {
        when(transactionRepository.findByIdAndUser(1L, testUser))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> transactionService.deleteTransaction(1L, testUser));
    }
}