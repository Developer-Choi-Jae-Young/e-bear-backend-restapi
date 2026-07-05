package com.example.ebearrestapi.infra.toss;

import com.example.ebearrestapi.dto.request.TossCancelDto;
import com.example.ebearrestapi.dto.request.TossConfirmDto;
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
public class TossPaymentApi {

    @Value("${toss.api.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 결제 승인 요청
     */
    public JsonNode confirm(TossConfirmDto confirmDto) {
        HttpHeaders headers = createHeaders();
        HttpEntity<TossConfirmDto> entity = new HttpEntity<>(confirmDto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm", entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (HttpStatusCodeException e) {
            log.error("토스 승인 에러 응답: {}", e.getResponseBodyAsString());
            throw new IllegalArgumentException(e.getResponseBodyAsString());
        } catch (JsonProcessingException e) {
            log.error("토스 통신 중 알 수 없는 에러 발생", e);
            throw new RuntimeException("결제 서버 통신 에러");
        }
    }

    /**
     * 결제 취소 요청
     */
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
