package com.example.ebearrestapi.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossConfirmDto {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
