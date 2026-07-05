package com.example.ebearrestapi.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final String errorCode;

    public PaymentException(String errorCode, String message) {
        super(message); //RuntimeException에 메시지 전달
        this.errorCode = errorCode;
    }
}
