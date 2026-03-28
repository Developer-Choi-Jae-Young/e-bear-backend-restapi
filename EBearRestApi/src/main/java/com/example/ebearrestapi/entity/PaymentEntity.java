package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PAYMENT")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentNo;

    // 토스페이먼츠에서 발급하는 결제 고유 키 (환불/조회 시 필수)
    @Column(length = 200, unique = true)
    private String paymentKey;

    // 주문번호
    @Column(nullable = false, unique = true)
    private String orderId;

    // 결제 상태 (READY, DONE, CANCELED 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    // 실제 결제 승인 일시 (토스 API의 approvedAt 값)
    private LocalDateTime approvedAt;
    
    @Column(nullable = false)
    private Integer paymentAmount;
}
