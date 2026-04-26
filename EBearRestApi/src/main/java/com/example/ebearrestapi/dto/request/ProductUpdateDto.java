package com.example.ebearrestapi.dto.request;

import com.example.ebearrestapi.etc.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductUpdateDto {
    private Long productId;
    private String productName;
    private String description;
    private Integer deliveryPrice;
    private Integer deliveryDays;
    private ProductStatus productStatus;
    private Long categoryId;
    private String title;
    private String content;
    private List<ProductOptionDto> productOptions;
}
