package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.CategorySaveDto;
import com.example.ebearrestapi.dto.request.CategoryUpdateDto;
import com.example.ebearrestapi.dto.response.CategoryListResultDto;
import com.example.ebearrestapi.dto.response.CategorySaveResultDto;
import com.example.ebearrestapi.dto.response.CategoryUpdateResultDto;
import com.example.ebearrestapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/save")
    public ResponseEntity<?> saveCategory(@RequestBody CategorySaveDto categorySaveDto) {
        CategorySaveResultDto categorySaveResultDto = categoryService.save(categorySaveDto);
        return ResponseEntity.status(HttpStatus.OK).body(categorySaveResultDto);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listCategory() {
        List<CategoryListResultDto> categoryListResult = categoryService.listCategory();
        return ResponseEntity.status(HttpStatus.OK).body(categoryListResult);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCategory(@RequestBody CategoryUpdateDto categoryUpdateDto) {
        CategoryUpdateResultDto categoryUpdateResult = categoryService.updateCategory(categoryUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body(categoryUpdateResult);
    }
}
