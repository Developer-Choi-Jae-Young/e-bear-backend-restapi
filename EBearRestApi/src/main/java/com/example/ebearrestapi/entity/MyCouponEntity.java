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
public class MyCouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myCouponId;

    @ManyToOne
    @JoinColumn(name = "couponNo", nullable = false)
    private CouponEntity coupon;

    @ManyToOne
    @JoinColumn(name = "userNo", nullable = false)
    private UserEntity user;

    private boolean isUsed;

    public void use() {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용 처리된 쿠폰입니다.");
        }
        this.isUsed = true;
    }
}
