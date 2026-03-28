package com.example.ebearrestapi.dto.request;

import lombok.Data;

@Data
public class OrderDto {
    private boolean isCart;
    private String address;
    private String tel;
    private String email;
    private String deliveryRequired;
    private Long productOptionNo;
    private Integer quantity;
    private Long cartNo;
    private Long couponNo;
    private Long orderId;
}
