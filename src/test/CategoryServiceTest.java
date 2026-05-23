package com.finance.manager.service;

import com.finance.manager.dto.request.CreateCategoryRequest;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private CategoryService categoryService;

    private User testUser;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        customCategory = new Category();
        customCategory.setId(10L);
        customCategory.setName("MyCategory");
        customCategory.setType(TransactionType.EXPENSE);
        customCategory.setUser(testUser);
        customCategory.setCustom(true);
        customCategory.setDeleted(false);
    }

    @Test
    void getAllCategories_ReturnsList() {
        when(categoryRepository.findAllVisibleToUser(testUser))
            .thenReturn(List.of(customCategory));

        CategoryListResponse response = categoryService.getAllCategories(testUser);

        assertEquals(1, response.getCategories().size());
        assertEquals("MyCategory", response.getCategories().get(0).getName());
    }

    @Test
    void createCategory_Success() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("NewCat");
        request.setType(TransactionType.INCOME);

        when(categoryRepository.existsByNameAndUser("NewCat", testUser)).thenReturn(false);
        when(categoryRepository.save(any())).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(request, testUser);

        assertNotNull(response);
        verify(categoryRepository).save(any());
    }

    @Test
    void createCategory_Duplicate_ThrowsConflict() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Existing");
        request.setType(TransactionType.EXPENSE);

        when(categoryRepository.existsByNameAndUser("Existing", testUser)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
            () -> categoryService.createCategory(request, testUser));
    }

    @Test
    void deleteCategory_NotInUse_HardDeletes() {
        when(categoryRepository.findByNameAndUserAndIsCustomTrue("MyCategory", testUser))
            .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        MessageResponse response = categoryService.deleteCategory("MyCategory", testUser);

        assertEquals("Category deleted successfully", response.getMessage());
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void deleteCategory_InUse_SoftDeletes() {
        when(categoryRepository.findByNameAndUserAndIsCustomTrue("MyCategory", testUser))
            .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        categoryService.deleteCategory("MyCategory", testUser);

        assertTrue(customCategory.isDeleted());
        verify(categoryRepository).save(customCategory);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_NotFound_ThrowsNotFound() {
        when(categoryRepository.findByNameAndUserAndIsCustomTrue("Ghost", testUser))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> categoryService.deleteCategory("Ghost", testUser));
    }
}