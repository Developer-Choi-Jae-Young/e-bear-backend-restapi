package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.CategorySaveDto;
import com.example.ebearrestapi.dto.request.CategoryUpdateDto;
import com.example.ebearrestapi.dto.response.CategoryListResultDto;
import com.example.ebearrestapi.dto.response.CategorySaveResultDto;
import com.example.ebearrestapi.dto.response.CategoryUpdateResultDto;
import com.example.ebearrestapi.dto.response.ParentCategory;
import com.example.ebearrestapi.entity.CategoryEntity;
import com.example.ebearrestapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategorySaveResultDto save(CategorySaveDto categorySaveDto) {
        CategoryEntity parentCategory = null;

        if (categorySaveDto.getParentId() != null) {
            parentCategory = categoryRepository.findById(categorySaveDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("parent category not found"));
        }

        CategoryEntity category = CategoryEntity.builder().categoryName(categorySaveDto.getCategoryName()).parent(parentCategory).build();
        CategoryEntity newCategory = categoryRepository.save(category);

        return CategorySaveResultDto.of(newCategory);
    }

    public List<CategoryListResultDto> listCategoryPage(Pageable pageable) {
        Page<CategoryEntity> categoryList = categoryRepository.findAllByChildrenListIsEmpty(pageable);

        return categoryList.map(CategoryListResultDto::of).getContent();
    }

    public List<CategoryListResultDto> listCategory() {
        List<CategoryEntity> categoryList = categoryRepository.findAllByParentIsNull();

        return categoryList.stream().map(CategoryListResultDto::of).toList();
    }

    @Transactional
    public CategoryUpdateResultDto updateCategory(CategoryUpdateDto categoryUpdateDto) {
        CategoryEntity category = categoryRepository.findById(categoryUpdateDto.getCategoryId()).orElseThrow(() -> new RuntimeException("category not found"));
        category.setCategoryName(categoryUpdateDto.getCategoryName());

        return CategoryUpdateResultDto.builder()
                .categoryName(categoryUpdateDto.getCategoryName())
                .parentCategory(ParentCategory.from(category.getParent()))
                .build();
    }
}
