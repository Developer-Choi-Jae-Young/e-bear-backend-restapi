package com.example.ebearrestapi.dto.request;

import lombok.Data;

@Data
public class CartDto {
    private Long cartNo;
    private Integer quantity = 1;
    private Long userNo;
    private Long productNo;
    private Long productOptionNo;
}
