package com.example.ebearrestapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductOptionDto {
    private Long productOptionId;
    private String productOptionName;
    private String productOptionValue;
    private Integer productPrice;
    private int quantity;
    private int couponId;
}
