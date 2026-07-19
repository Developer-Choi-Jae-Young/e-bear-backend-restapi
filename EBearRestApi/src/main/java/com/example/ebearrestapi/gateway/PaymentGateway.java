package com.example.ebearrestapi.gateway;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.etc.PgProvider;
import com.example.ebearrestapi.gateway.dto.PaymentResponseDto;

public interface PaymentGateway {
    // Gateway 구현체가 해당 PG사 타입을 지원하는지 판별
    boolean supports(PgProvider pgProvider);
    // 공통 승인 규격
    PaymentResponseDto confirm(PaymentConfirmDto confirmDto);
    // 공통 취소 규격
    void cancel(String paymentKey, String reason);
}
