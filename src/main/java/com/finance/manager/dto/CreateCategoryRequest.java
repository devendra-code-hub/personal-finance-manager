package com.finance.manager.dto.request;

import com.finance.manager.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    private TransactionType type;
}