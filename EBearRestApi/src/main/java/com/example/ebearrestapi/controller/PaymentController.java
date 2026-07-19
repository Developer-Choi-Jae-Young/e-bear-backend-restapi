package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.dto.request.TossWebhookDto;
import com.example.ebearrestapi.dto.request.UserDto;
import com.example.ebearrestapi.dto.response.PaymentDetailsDto;
import com.example.ebearrestapi.exception.PaymentException;
import com.example.ebearrestapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/ready")
    public ResponseEntity<?> readyOrder(@RequestBody PaymentDto paymentDto,
                                        @AuthenticationPrincipal UserDto userDto) {
        paymentService.readyPayment(paymentDto);
        return ResponseEntity.ok().build();
//        return ResponseEntity.ok(Map.of("pgProvider", pgProvider.name())); //나중에 다중 PG사 사용 시
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmDto paymentConfirmDto) {
        // 토스 API 승인 및 DB 상태 변경(READY -> DONE) 처리
        paymentService.confirmPayment(paymentConfirmDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/details")
    public ResponseEntity<PaymentDetailsDto> getPaymentDetails(@RequestParam String orderPaymentId) {
        PaymentDetailsDto details = paymentService.getPaymentDetails(orderPaymentId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody TossWebhookDto webhookDto) {

        // 이벤트 타입이 결제 상태 변경인지 확인
        if ("PAYMENT_STATUS_CHANGED".equals(webhookDto.getEventType())) {
            String currentStatus = webhookDto.getData().getStatus();
            String orderId = webhookDto.getData().getOrderId();

            // 결제가 비정상 종료되거나 실패한 경우
            if ("ABORTED".equals(currentStatus) || "CANCELED".equals(currentStatus)) {
                // DB에서 해당 orderId를 찾아 상태를 변경하고,
                // TODO: 묶여있던 상품 재고를 다시 원상복구(+1) 시키는 로직 작성 필요
                // TODO: => 해당 로직은 재영님 코드 통합 후 진행 예정
                paymentService.handleAbortedWebhook(orderId);
            }
        }
        // 토스페이먼츠 서버에 "웹훅 잘 받았음"을 알리기 위해 반드시 200 응답을 10초 이내에 리턴 필요
        return ResponseEntity.ok().build();
    }
}
