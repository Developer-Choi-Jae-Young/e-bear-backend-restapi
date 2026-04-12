package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryProductResult {
    private Long categoryId;
    private String categoryName;
    private CategoryProductResult child;

    public static CategoryProductResult from(CategoryEntity category) {
        return CategoryProductResult.builder()
                .categoryId(category.getCategoryNo())
                .categoryName(category.getCategoryName())
                .child(from(category.getParent()))
                .build();
    }
}
