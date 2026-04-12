package com.example.ebearrestapi.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderDto {
    private String address;
    private String tel;
    private String email;
    private String deliveryRequired;
    private List<OrderProductOptionDto> productOptionList;
    private Long orderId;
}
