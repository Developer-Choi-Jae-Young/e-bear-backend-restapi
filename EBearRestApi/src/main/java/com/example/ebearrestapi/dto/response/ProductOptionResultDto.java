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
public class ProductOptionResultDto {
    private Long productOptionId;
    private String productOptionName;
    private String productName;
    private int quantity;
    private Long couponNo;
    private int price;
    private Long seller;
}
