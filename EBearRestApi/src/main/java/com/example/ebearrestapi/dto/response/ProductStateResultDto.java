package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.etc.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStateResultDto {
    private String stateName;
    private String stateCode;

    public static ProductStateResultDto of(ProductStatus productStatus) {
        return ProductStateResultDto.builder()
                .stateName(productStatus.getName())
                .stateCode(productStatus.name())
                .build();
    }
}
