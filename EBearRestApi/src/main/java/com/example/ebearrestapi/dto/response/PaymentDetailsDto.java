package com.example.ebearrestapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentDetailsDto {
    private String orderPaymentId;
    private String paymentStatus;
    private Integer totalAmount;
    private Integer usedPoint;
    private List<PaymentProductDto> products;
    private LocalDateTime approvedAt;
}
