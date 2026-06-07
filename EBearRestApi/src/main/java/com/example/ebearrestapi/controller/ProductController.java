package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.ProductDeleteDto;
import com.example.ebearrestapi.dto.request.ProductSaveDto;
import com.example.ebearrestapi.dto.request.ProductUpdateDto;
import com.example.ebearrestapi.dto.response.*;
import com.example.ebearrestapi.service.FileService;
import com.example.ebearrestapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<?> listProduct(Pageable pageable,
                                         @RequestParam(value = "type", required = false) String type,
                                         @RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "categoryId", required = false) Long categoryId) {
        ProductCategoryResponseDto productCategoryResponseDto = productService.listProduct(pageable, type, keyword, categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(productCategoryResponseDto);
    }

    @GetMapping("/list/admin")
    public ResponseEntity<?> listProductAdmin(Pageable pageable,
                                         @RequestParam(value = "type", required = false) String type,
                                         @RequestParam(value = "keyword", required = false) String keyword,
                                         @AuthenticationPrincipal User user) {
        List<ProductListResultDto> productList = productService.listProductAdmin(pageable, user, type, keyword);
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/detail/{productId}")
    public ResponseEntity<?> detailProduct(@PathVariable Long productId) {
        ProductDetailResult productDetailResult = productService.detailProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(productDetailResult);
    }

    @PostMapping(value = "/save", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> saveProduct(@RequestPart("productSaveDto") ProductSaveDto productSaveDto,
                                         @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                         @AuthenticationPrincipal User user) {
         ProductSaveResultDto productSaveResult = productService.saveProduct(productSaveDto, user);

         if (files != null && !files.isEmpty()) {
             files.forEach(file -> {
                 try {
                     fileService.uploadImage(file);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             });
         }

         return ResponseEntity.status(HttpStatus.CREATED).body(productSaveResult);
    }

    @PostMapping(value = "/update", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateProduct(@RequestPart("productUpdateDto") ProductUpdateDto productUpdateDto
                                            , @RequestPart(value = "files", required = false) List<MultipartFile> files
                                            , @AuthenticationPrincipal User user) {
        ProductUpdateResultDto productUpdateResult = productService.updateProduct(productUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body(productUpdateResult);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteProduct(@RequestBody ProductDeleteDto productDeleteDto) {
        productService.deleteProduct(productDeleteDto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
