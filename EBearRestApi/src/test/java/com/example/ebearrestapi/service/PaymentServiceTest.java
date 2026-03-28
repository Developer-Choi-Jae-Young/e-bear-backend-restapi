package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 준비(ready) 성공 테스트")
    void readyPayment_Success() {
        // given (준비)
        PaymentDto dto = new PaymentDto();
        dto.setPaymentAmount(50000);

        PaymentEntity savedEntity = PaymentEntity.builder()
                .orderId("test-order-id-123")
                .paymentAmount(50000)
                .build();

        // save하면 savedEntity를 반환
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedEntity);

        // when (실행)
        String orderId = paymentService.readyPayment(dto);

        // then (검증)
        assertNotNull(orderId);
        assertEquals("test-order-id-123", orderId);
        verify(paymentRepository, times(1)).save(any(PaymentEntity.class)); // save가 1번 호출되었는지 확인
    }

    @Test
    @DisplayName("결제 승인(confirm) 성공 테스트")
    void confirmPayment_Success() {
        // given (준비)
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setOrderId("test-order-id-123");
        confirmDto.setPaymentKey("test-payment-key");
        confirmDto.setAmount(50000);

        // DB에 저장되어 있던 결제 대기 상태의 데이터
        PaymentEntity existingPayment = PaymentEntity.builder()
                .orderId("test-order-id-123")
                .paymentAmount(50000)
                .paymentStatus(PaymentStatus.READY)
                .build();

        // 주문번호로 찾으면 existingPayment 반환
        when(paymentRepository.findByOrderId("test-order-id-123")).thenReturn(Optional.of(existingPayment));

        // 토스 서버가 200 OK를 반환한다고 가정
        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"status\":\"DONE\"}", HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        // when (실행)
        Object result = paymentService.confirmPayment(confirmDto);

        // then (검증)
        assertEquals("success", result);
        assertEquals(PaymentStatus.DONE, existingPayment.getPaymentStatus()); // 상태가 DONE으로 바뀌었는지 확인
        assertEquals("test-payment-key", existingPayment.getPaymentKey());
    }

    @Test
    @DisplayName("결제 승인 실패 - 금액 위조 발생 시 예외 발생")
    void confirmPayment_Fail_ForgedAmount() {
        // given
        PaymentConfirmDto confirmDto = new PaymentConfirmDto();
        confirmDto.setOrderId("test-order-id-123");
        confirmDto.setAmount(100); // 프론트엔드에서 조작된 금액 (100원)

        // DB에는 5만원으로 기록되어 있음
        PaymentEntity existingPayment = PaymentEntity.builder()
                .orderId("test-order-id-123")
                .paymentAmount(50000)
                .build();

        when(paymentRepository.findByOrderId("test-order-id-123")).thenReturn(Optional.of(existingPayment));

        // when & then (실행 및 예외 검증)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(confirmDto);
        });

        // 예외 메시지가 정확히 던져졌는지 확인
        assertTrue(exception.getMessage().contains("FORGED_AMOUNT"));
    }

}
