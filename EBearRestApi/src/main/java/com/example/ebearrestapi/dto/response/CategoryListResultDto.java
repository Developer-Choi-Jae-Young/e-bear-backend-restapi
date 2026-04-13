package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResultDto {
    private Long categoryId;
    private String categoryName;
    private List<CategoryListResultDto> childCategory;

    public static CategoryListResultDto of(CategoryEntity category) {
        return category != null ? CategoryListResultDto.builder()
                .categoryId(category.getCategoryNo())
                .categoryName(category.getCategoryName())
                .childCategory(category.getChildrenList().stream()
                        .map(CategoryListResultDto::of)
                        .toList())
                .build() : null;
    }
}
