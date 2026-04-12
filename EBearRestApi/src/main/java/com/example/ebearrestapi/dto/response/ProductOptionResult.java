package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.ProductOptionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOptionResult {
    private Long productOptionId;
    private String productOptionName;
    private Integer productOptionPrice;
    private Integer productOptionInventory;
    private String productOptionValue;

    public static ProductOptionResult from(ProductOptionEntity productOptionEntity) {
        return ProductOptionResult.builder()
                .productOptionId(productOptionEntity.getProductOptionNo())
                .productOptionName(productOptionEntity.getProductOptionName())
                .productOptionPrice(productOptionEntity.getProductOptionPrice())
                .productOptionInventory(productOptionEntity.getProductOptionPrice())
                .productOptionValue(productOptionEntity.getProductOptionValue())
                .build();
    }
}
