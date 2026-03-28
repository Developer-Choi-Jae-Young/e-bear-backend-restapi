package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.dto.request.UserDto;
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
                                          @AuthenticationPrincipal UserDto userDto){

        String orderId = paymentService.readyPayment(paymentDto);

        return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmDto paymentConfirmDto) {
        // 토스 API 승인 및 DB 상태 변경(READY -> DONE) 처리
        try {
            Object result = paymentService.confirmPayment(paymentConfirmDto);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // 토스 API 실패 사유 프론트로 전달 (400 상태코드)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 기타 서버 내부 에러
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
