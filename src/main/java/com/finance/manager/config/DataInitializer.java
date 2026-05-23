package com.finance.manager.config;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.TransactionType;
import com.finance.manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds the default categories when the app starts.
 * Why CommandLineRunner? It runs after Spring context is fully initialized,
 * so repositories are ready to use.
 *
 * Default categories per assignment spec:
 * INCOME: Salary
 * EXPENSE: Food, Rent, Transportation, Entertainment, Healthcare, Utilities
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDefaultCategories(CategoryRepository categoryRepository) {
        return args -> {
            // Only seed if not already present (in case of restart with persistent DB)
            if (categoryRepository.count() == 0) {
                List<Category> defaults = List.of(
                    makeDefault("Salary", TransactionType.INCOME),
                    makeDefault("Food", TransactionType.EXPENSE),
                    makeDefault("Rent", TransactionType.EXPENSE),
                    makeDefault("Transportation", TransactionType.EXPENSE),
                    makeDefault("Entertainment", TransactionType.EXPENSE),
                    makeDefault("Healthcare", TransactionType.EXPENSE),
                    makeDefault("Utilities", TransactionType.EXPENSE)
                );
                categoryRepository.saveAll(defaults);
            }
        };
    }

    private Category makeDefault(String name, TransactionType type) {
        Category c = new Category();
        c.setName(name);
        c.setType(type);
        c.setUser(null); // null user = default category (visible to all)
        c.setCustom(false);
        c.setDeleted(false);
        return c;
    }
}