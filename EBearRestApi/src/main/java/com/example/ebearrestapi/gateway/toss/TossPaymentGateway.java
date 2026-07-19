package com.example.ebearrestapi.gateway.toss;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.TossCancelDto;
import com.example.ebearrestapi.dto.request.TossConfirmDto;
import com.example.ebearrestapi.etc.PgProvider;
import com.example.ebearrestapi.gateway.PaymentGateway;
import com.example.ebearrestapi.gateway.dto.PaymentResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossPaymentGateway implements PaymentGateway {

    @Value("${toss.api.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(PgProvider pgProvider) {
        return pgProvider == PgProvider.TOSS;
    }

    /**
     * 결제 승인 요청
     */
    @Override
    public PaymentResponseDto confirm(PaymentConfirmDto confirmDto) {
        // 공통 DTO를 토스 전용 DTO로 매핑
        TossConfirmDto tossConfirmDto = TossConfirmDto.builder()
                .paymentKey(confirmDto.getPaymentKey())
                .orderId(confirmDto.getOrderId())
                .amount(confirmDto.getAmount())
                .build();

        HttpHeaders headers = createHeaders();
        HttpEntity<TossConfirmDto> entity = new HttpEntity<>(tossConfirmDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm", entity, String.class);
            JsonNode body = objectMapper.readTree(response.getBody());

            return PaymentResponseDto.builder()
                    .isSuccess("DONE".equals(body.get("status").asText()))
                    .transactionId(body.get("paymentKey").asText())
                    .rawResponse(response.getBody())
                    .build();
        } catch (HttpStatusCodeException e) {
            log.error("토스 승인 에러 응답: {}", e.getResponseBodyAsString());
            return PaymentResponseDto.builder()
                    .isSuccess(false)
                    .errorMessage(e.getResponseBodyAsString())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("토스 통신 중 알 수 없는 에러 발생", e);
            return PaymentResponseDto.builder()
                    .isSuccess(false)
                    .errorMessage("결제 서버 통신 에러")
                    .build();
        }
    }

    /**
     * 결제 취소 요청
     */
    @Override
    public void cancel(String paymentKey, String reason) {
        log.info("토스 결제 취소 요청: paymentKey={}", paymentKey);

        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        TossCancelDto cancelDto = TossCancelDto.builder().cancelReason(reason).build();

        HttpEntity<TossCancelDto> entity = new HttpEntity<>(cancelDto, createHeaders());

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            log.error("토스 결제 취소 실패 : paymentKey={}, reason={}", paymentKey, reason);
        }
    }

    /**
     * 공통 헤더 생성(Base63 인증 포함)
     */
    private HttpHeaders createHeaders() {
        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
