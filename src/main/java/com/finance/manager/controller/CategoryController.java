package com.finance.manager.controller;

import com.finance.manager.dto.request.CreateCategoryRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.service.AuthService;
import com.finance.manager.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(categoryService.getAllCategories(user));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            HttpServletRequest httpRequest) {
        User user = authService.getCurrentUser(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request, user));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(
            @PathVariable String name,
            HttpServletRequest request) {
        User user = authService.getCurrentUser(request);
        return ResponseEntity.ok(categoryService.deleteCategory(name, user));
    }
}