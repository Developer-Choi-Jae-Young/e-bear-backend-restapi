package com.example.ebearrestapi.dto.response;

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
public class ProductDetailResult {
    private Long productId;
    private String productName;
    private String thumbnail;
    private String content;
    private String seller;
    private String sellerImg;
    private Integer deliveryPrice;
    private Integer deliveryDays;
    private ProductStatus productStatus;
    private CategoryProductResult category;
    private List<ProductOptionResult> productOptions;
    private List<ReviewProductResult> reviews;
    private List<QnAProductResult> qnas;
}
