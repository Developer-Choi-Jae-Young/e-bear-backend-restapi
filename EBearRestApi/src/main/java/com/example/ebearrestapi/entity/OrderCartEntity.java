package com.example.ebearrestapi.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ORDER_CART")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("OrderCart")
public class OrderCartEntity extends OrderPaymentEntity {
    @ManyToOne
    @JoinColumn(name = "cartNo")
    private CartEntity cart;
}
