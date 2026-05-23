package com.example.ebearrestapi.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderSaveReqDto {
    private List<OrderProductOptionReqDto> productOptionList;
}
