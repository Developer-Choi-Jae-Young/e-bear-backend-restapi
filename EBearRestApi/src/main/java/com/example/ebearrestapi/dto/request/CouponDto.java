package com.example.ebearrestapi.dto.request;

import com.example.ebearrestapi.etc.CouponType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponDto {
    private Long couponNo;
    private String couponName;
    private Integer couponQuantity;
    private Double salePrice;
    private CouponType couponType;
    private LocalDateTime expiredDate;
    private Integer useAbledPrice;
    private Long categoryNo;
    private Long userNo;
}
