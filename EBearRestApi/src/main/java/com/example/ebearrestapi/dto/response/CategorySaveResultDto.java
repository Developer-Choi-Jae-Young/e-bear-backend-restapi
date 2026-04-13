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
public class CategorySaveResultDto {
    private Long categoryId;
    private String categoryName;
    private List<CategorySaveResultDto> childCategory;

    public static CategorySaveResultDto of(CategoryEntity category) {
        return category != null ? CategorySaveResultDto.builder()
                .categoryId(category.getCategoryNo())
                .categoryName(category.getCategoryName())
                .childCategory(category.getChildrenList().stream()
                        .map(CategorySaveResultDto::of)
                        .toList())
                .build() : null;
    }
}
