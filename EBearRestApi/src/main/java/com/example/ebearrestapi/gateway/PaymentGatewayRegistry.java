package com.example.ebearrestapi.gateway;

import com.example.ebearrestapi.etc.PgProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRegistry {
    private final List<PaymentGateway> gateways;
    // 서비스가 요청한  PgType에 매칭되는 적합한 빈을 반환
    public PaymentGateway getGateway(PgProvider pgProvider) {
        return gateways.stream()
                .filter(g -> g.supports(pgProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 PG사 유형입니다: " + pgProvider));
    }
}
