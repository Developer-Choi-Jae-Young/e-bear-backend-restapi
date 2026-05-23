package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.etc.OrderStatus;
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
public class OrderSelectResultDto {
    private Long orderItemId;
    private String address;
    private String phone;
    private String email;
    private String deliveryRequired;
    private int point;
    private OrderStatus orderStatus;
    private List<ProductOptionResultDto> productOptions;
}
