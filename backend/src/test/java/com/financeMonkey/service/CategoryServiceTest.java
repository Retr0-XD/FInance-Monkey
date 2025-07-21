package com.financeMonkey.service;

import com.financeMonkey.dto.CategoryDto;
import com.financeMonkey.model.Category;
import com.financeMonkey.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;
    private Category childCategory;
    private UUID parentId;
    private UUID childId;

    @BeforeEach
    void setUp() {
        parentId = UUID.randomUUID();
        childId = UUID.randomUUID();

        parentCategory = new Category();
        parentCategory.setId(parentId);
        parentCategory.setName("Parent Category");
        parentCategory.setIcon("icon-parent");
        parentCategory.setColorCode("#FF0000");

        childCategory = new Category();
        childCategory.setId(childId);
        childCategory.setName("Child Category");
        childCategory.setIcon("icon-child");
        childCategory.setColorCode("#00FF00");
        childCategory.setParentCategory(parentCategory);
    }

    @Test
    void getAllRootCategories_ShouldReturnOnlyRootCategories() {
        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(Arrays.asList(parentCategory));

        List<CategoryDto> result = categoryService.getAllRootCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Parent Category", result.get(0).getName());
        assertNull(result.get(0).getParentCategoryId());
        verify(categoryRepository, times(1)).findByParentCategoryIsNull();
    }

    @Test
    void getSubcategories_WhenParentExists_ShouldReturnChildCategories() {
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findByParentCategory(parentCategory)).thenReturn(Arrays.asList(childCategory));

        List<CategoryDto> result = categoryService.getSubcategories(parentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Child Category", result.get(0).getName());
        assertEquals(parentId, result.get(0).getParentCategoryId());
        verify(categoryRepository, times(1)).findById(parentId);
        verify(categoryRepository, times(1)).findByParentCategory(parentCategory);
    }

    @Test
    void getSubcategories_WhenParentDoesNotExist_ShouldThrowException() {
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getSubcategories(UUID.randomUUID()));
        verify(categoryRepository, times(1)).findById(any(UUID.class));
        verify(categoryRepository, never()).findByParentCategory(any(Category.class));
    }

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        when(categoryRepository.findById(childId)).thenReturn(Optional.of(childCategory));

        CategoryDto result = categoryService.getCategoryById(childId);

        assertNotNull(result);
        assertEquals(childId, result.getId());
        assertEquals("Child Category", result.getName());
        assertEquals(parentId, result.getParentCategoryId());
        verify(categoryRepository, times(1)).findById(childId);
    }

    @Test
    void getCategoryById_WhenNotExists_ShouldThrowException() {
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(UUID.randomUUID()));
        verify(categoryRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void createCategory_WithoutParent_ShouldCreateRootCategory() {
        CategoryDto newCategoryDto = new CategoryDto();
        newCategoryDto.setName("New Category");
        newCategoryDto.setIcon("icon-new");
        newCategoryDto.setColorCode("#0000FF");

        Category newCategory = new Category();
        newCategory.setId(UUID.randomUUID());
        newCategory.setName("New Category");
        newCategory.setIcon("icon-new");
        newCategory.setColorCode("#0000FF");

        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

        CategoryDto result = categoryService.createCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        assertEquals("icon-new", result.getIcon());
        assertEquals("#0000FF", result.getColorCode());
        assertNull(result.getParentCategoryId());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryRepository, never()).findById(any(UUID.class));
    }

    @Test
    void createCategory_WithParent_ShouldCreateChildCategory() {
        UUID parentUuid = UUID.randomUUID();
        
        CategoryDto newCategoryDto = new CategoryDto();
        newCategoryDto.setName("New Child");
        newCategoryDto.setIcon("icon-new-child");
        newCategoryDto.setColorCode("#0000FF");
        newCategoryDto.setParentCategoryId(parentUuid);

        Category parentCat = new Category();
        parentCat.setId(parentUuid);
        parentCat.setName("Parent");

        Category newCategory = new Category();
        newCategory.setId(UUID.randomUUID());
        newCategory.setName("New Child");
        newCategory.setIcon("icon-new-child");
        newCategory.setColorCode("#0000FF");
        newCategory.setParentCategory(parentCat);

        when(categoryRepository.findById(parentUuid)).thenReturn(Optional.of(parentCat));
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);

        CategoryDto result = categoryService.createCategory(newCategoryDto);

        assertNotNull(result);
        assertEquals("New Child", result.getName());
        assertEquals(parentUuid, result.getParentCategoryId());
        verify(categoryRepository, times(1)).findById(parentUuid);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}
