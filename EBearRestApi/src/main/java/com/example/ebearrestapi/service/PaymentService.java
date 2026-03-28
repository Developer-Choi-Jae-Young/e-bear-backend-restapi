package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.PaymentType;
import com.example.ebearrestapi.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    public String readyPayment(PaymentDto paymentDto) {
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(UUID.randomUUID().toString())
                .paymentAmount(paymentDto.getPaymentAmount())
                .paymentStatus(PaymentStatus.READY)
                .paymentType(PaymentType.KAKAO_PAY)
                .build();

        PaymentEntity savePayment = paymentRepository.save(payment);

        return savePayment.getOrderId();
    }

    @Transactional
    public Object confirmPayment(PaymentConfirmDto paymentConfirmDto) {
        // DB 주문 찾기
        PaymentEntity payment = paymentRepository.findByOrderId(paymentConfirmDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("{\"code\":\"NOT_FOUND\", \"message\":\"주문 내역을 찾을 수 없습니다.\"}"));
        // 금액 검증 (위조 방지)
        if (!payment.getPaymentAmount().equals(paymentConfirmDto.getAmount())) {
            throw new RuntimeException("{\"code\":\"FORGED_AMOUNT\", \"message\":\"결제 금액이 조작되었습니다.\"}");
        }
        // 토스페이먼츠 승인 API 호출
//        RestTemplate restTemplate = new RestTemplate();
        String secretKey = "test_sk_LlDJaYngroj5o7bqzPOnVezGdRpX";
        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 토스 서버로 보낼 JSON 바디 데이터
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentConfirmDto.getPaymentKey());
        requestBody.put("orderId", paymentConfirmDto.getOrderId());
        requestBody.put("amount", paymentConfirmDto.getAmount());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 정상 승인 시도
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    requestEntity,
                    String.class
            );

            // 상태 변경 (성공)
            payment.setPaymentStatus(PaymentStatus.DONE);
            payment.setPaymentKey(paymentConfirmDto.getPaymentKey());
            payment.setApprovedAt(LocalDateTime.now());

            return "success";

        } catch (HttpStatusCodeException e) {
            // 토스에서 에러(잔액부족 등)를 던졌을 때
            payment.setPaymentStatus(PaymentStatus.ABORTED); // 실패 상태로 저장
            paymentRepository.save(payment);

            // 토스가 준 에러 JSON(code, message 포함)을 그대로 던짐
            throw new IllegalArgumentException(e.getResponseBodyAsString());
        }
    }
}
