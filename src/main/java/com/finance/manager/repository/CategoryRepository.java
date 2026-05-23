package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Get all categories visible to a user: default categories + their own custom ones (not deleted)
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user = :user) AND c.deleted = false")
    List<Category> findAllVisibleToUser(User user);

    // Find a specific non-deleted category by name visible to user
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user IS NULL OR c.user = :user) AND c.deleted = false")
    Optional<Category> findByNameAndUser(String name, User user);

    // Check if custom category name already exists for this user (including deleted ones to avoid reuse)
    boolean existsByNameAndUser(String name, User user);

    // Find custom category by name for deletion
    Optional<Category> findByNameAndUserAndIsCustomTrue(String name, User user);
}