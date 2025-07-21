package com.financeMonkey.repository;

import com.financeMonkey.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategory(Category parentCategory);
    boolean existsByName(String name);
}
