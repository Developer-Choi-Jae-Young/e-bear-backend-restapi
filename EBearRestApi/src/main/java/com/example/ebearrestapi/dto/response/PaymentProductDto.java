package com.example.ebearrestapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProductDto {
    private String productName;
    private String optionName;
    private Integer quantity;
    private Integer price;
}
