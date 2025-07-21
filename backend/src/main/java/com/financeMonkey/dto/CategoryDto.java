package com.financeMonkey.dto;

import com.financeMonkey.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private UUID id;
    private String name;
    private UUID parentCategoryId;
    private String icon;
    private String colorCode;
    
    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .colorCode(category.getColorCode())
                .build();
        
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
        }
        
        return dto;
    }
}
