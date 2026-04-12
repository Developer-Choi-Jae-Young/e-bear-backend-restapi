package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentCategory {
    private Long categoryId;
    private String categoryName;
    private ParentCategory parentCategory;

    public static ParentCategory from(CategoryEntity category) {
        return ParentCategory.builder()
                .categoryId(category.getCategoryNo())
                .categoryName(category.getCategoryName())
                .parentCategory(from(category.getParent()))
                .build();
    }
}
