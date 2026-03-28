package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("Order")
public class OrderEntity extends OrderPaymentEntity {
    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "couponNo")
    private CouponEntity coupon;

    @ManyToOne
    @JoinColumn(name = "userNo")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "productOptionNo")
    private ProductOptionEntity productOption;
}
