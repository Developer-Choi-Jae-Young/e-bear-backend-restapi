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
public class ChildCategory {
    private Long categoryId;
    private String categoryName;
}
