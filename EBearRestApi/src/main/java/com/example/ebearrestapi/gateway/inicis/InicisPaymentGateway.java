package com.example.ebearrestapi.gateway.inicis;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.etc.PgProvider;
import com.example.ebearrestapi.gateway.PaymentGateway;
import com.example.ebearrestapi.gateway.dto.PaymentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class InicisPaymentGateway implements PaymentGateway {
    @Override
    public boolean supports(PgProvider pgProvider) {
        return pgProvider == PgProvider.INICIS;
    }

    @Override
    public PaymentResponseDto confirm(PaymentConfirmDto confirmDto) {
        // 아직 로직을 타지 않으므로 예외 던지기
        throw new UnsupportedOperationException("KG이니시스 결제는 현재 준비 중입니다.");
    }

    @Override
    public void cancel(String paymentKey, String reason) {
        // 빈 메서드
    }
}
