package com.example.ebearrestapi.gateway.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponseDto {
    private boolean isSuccess;      // 승인 성공 여부
    private String transactionId;   // PG사별 거래 고유번호
    private String rawResponse;     // PG사 실제 원본 응답값
    private String errorMessage;    // 실패 시 상세 사유
}
