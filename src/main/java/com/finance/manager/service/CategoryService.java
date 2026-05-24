package com.finance.manager.service;

import com.finance.manager.dto.request.CreateCategoryRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryListResponse getAllCategories(User user) {
        List<CategoryResponse> categories = categoryRepository.findAllVisibleToUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new CategoryListResponse(categories);
    }

    public CategoryResponse createCategory(CreateCategoryRequest request, User user) {
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new DuplicateResourceException("Category with this name already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(user);
        category.setCustom(true);
        category.setDeleted(false);

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public MessageResponse deleteCategory(String name, User user) {
        // Find the custom category for this user
        Category category = categoryRepository.findByNameAndUserAndIsCustomTrue(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        // 403: trying to delete a default category
        if (!category.isCustom()) {
            throw new ForbiddenException("Cannot delete default categories");
        }

        // 400: category is currently referenced by transactions — cannot delete
        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException("Cannot delete category that is currently in use by transactions");
        }

        categoryRepository.delete(category);
        return new MessageResponse("Category deleted successfully");
    }

    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getName(), c.getType(), c.isCustom());
    }
}