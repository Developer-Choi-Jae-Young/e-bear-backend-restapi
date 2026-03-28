package com.example.ebearrestapi.dto.request;

import lombok.Data;

@Data
public class PaymentConfirmDto {
    // 토스페이먼츠가 발급한 결제 고유 식별자
    private String paymentKey;
    // 생성해서 토스에게 줬다가 다시 돌려받은 주문번호 (UUID)
    private String orderId;
    // 사용자가 실제로 결제한 금액
    private Integer amount;
}
