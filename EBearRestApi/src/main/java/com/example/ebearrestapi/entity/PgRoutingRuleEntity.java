package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Entity
public class PgRoutingRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleNo;

    // 하나의 PG사를 바라봄
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pg_code")
    private PgProviderEntity pgProvider;

    private PaymentType paymentType; // CARD, EASY_PAY 등

    private BigDecimal feeRate;   // 수수료율
}
