package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.entity.OrderPaymentEntity;
import com.example.ebearrestapi.etc.OrderStatus;
import jakarta.persistence.criteria.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSelectListResultDto {
    private Long paymentId;
    private String orderPaymentId;
    private Long reviewId;
    private OrderStatus orderStatus;
    private List<ProductOptionDto> productOptions;
    private LocalDate orderDate;
    private String receiver;
    private String productName;
    private String seller;
}
