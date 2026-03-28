package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MyCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myCouponId;

    @ManyToOne
    @JoinColumn(name = "couponNo", nullable = false)
    private CouponEntity coupon;

    @ManyToOne
    @JoinColumn(name = "userNo", nullable = false)
    private UserEntity user;
}
