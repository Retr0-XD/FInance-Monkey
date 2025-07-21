package com.financeMonkey.service;

import com.financeMonkey.dto.CategoryDto;
import com.financeMonkey.model.Category;
import com.financeMonkey.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllRootCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getSubcategories(UUID parentCategoryId) {
        Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + parentCategoryId));
        
        return categoryRepository.findByParentCategory(parentCategory).stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(CategoryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setIcon(categoryDto.getIcon());
        category.setColorCode(categoryDto.getColorCode());
        
        if (categoryDto.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + categoryDto.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(UUID categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        category.setName(categoryDto.getName());
        category.setIcon(categoryDto.getIcon());
        category.setColorCode(categoryDto.getColorCode());
        
        if (categoryDto.getParentCategoryId() != null) {
            if (!categoryDto.getParentCategoryId().equals(categoryId)) { // Prevent self-reference
                Category parentCategory = categoryRepository.findById(categoryDto.getParentCategoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + categoryDto.getParentCategoryId()));
                category.setParentCategory(parentCategory);
            }
        } else {
            category.setParentCategory(null);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return CategoryDto.fromEntity(savedCategory);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }
}
