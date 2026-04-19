package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDER_PAYMENT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderPaymentId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String deliveryAddr;
    private String tel;
    private String email;
    private String deliveryRequired;

    @Builder.Default
    private boolean delYn = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userNo", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymentNo")
    private PaymentEntity payment;

    public void updateDeliveryInfo(String address, String tel, String email, String deliveryRequired) {
        this.deliveryRequired = deliveryRequired;
        this.email = email;
        this.deliveryAddr = address;
        this.tel = tel;
    }
}
