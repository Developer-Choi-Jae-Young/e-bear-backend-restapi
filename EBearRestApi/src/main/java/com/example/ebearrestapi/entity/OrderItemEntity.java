package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ORDER_ITEM")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(OrderItemId.class)
public class OrderItemEntity {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderPaymentId")
    private OrderPaymentEntity orderPayment;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productOptionNo")
    private ProductOptionEntity productOption;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "myCouponNo")
    private MyCouponEntity myCoupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userNo")
    private UserEntity user;
}