package com.example.ebearrestapi.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Payment 전용 예외
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<?> handlePaymentException(PaymentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", e.getErrorCode(),
                "message", e.getMessage()
        ));
    }

    // 토스 API에서 던져지는 규격화된 에러 응답 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        // 토스에서 보내준 에러 JSON 메시지 전달
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 그 외에 잡히지 않은 모든 런타임 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(Map.of(
                "code", "INTERNAL_SERVER_ERROR",
                "message", "서버 내부 오류가 발생했습니다."
        ));
    }
}
