package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.ProductSaveDto;
import com.example.ebearrestapi.dto.request.ProductUpdateDto;
import com.example.ebearrestapi.dto.response.*;
import com.example.ebearrestapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/list")
    public ResponseEntity<?> listProduct(Pageable pageable) {
        List<ProductListResultDto> productList = productService.listProduct(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(productList);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> detailProduct(Long productId) {
        ProductDetailResult productDetailResult = productService.detailProduct(productId);
        return ResponseEntity.status(HttpStatus.OK).body(productDetailResult);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveProduct(@RequestBody ProductSaveDto productSaveDto, @AuthenticationPrincipal User user) {
         ProductSaveResultDto productSaveResult = productService.saveProduct(productSaveDto, user);
         return ResponseEntity.status(HttpStatus.CREATED).body(productSaveResult);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestBody ProductUpdateDto productUpdateDto) {
        ProductUpdateResultDto productUpdateResult = productService.updateProduct(productUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body(productUpdateResult);
    }
}
