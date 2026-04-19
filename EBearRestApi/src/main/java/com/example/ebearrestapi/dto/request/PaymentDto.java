package com.example.ebearrestapi.dto.request;

import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.PaymentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private String paymentKey;   // 승인 후 채워짐
    private String orderId;      // 생성 시점부터 존재
    private PaymentStatus status; // 결제 상태 (READY -> DONE)
    private PaymentType type;     // 결제 수단 (CARD 등)
    private LocalDateTime approvedAt;   //결제 완료 일시
    private Integer paymentAmount;      //최종 결제 금액
    private Integer usePoint;           //임시 사용 포인트
}
