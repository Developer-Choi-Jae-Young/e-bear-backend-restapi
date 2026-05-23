package com.example.ebearrestapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductOptionReqDto {
    private Long productOptionId;
    private String optionSubject;
    private String productOptionValue;
    private Integer optionPrice;
    private int optionCount;
    private int couponId;
}
