package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "order_type")
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

    public void updateDeliveryInfo(String address, String tel, String email, String deliveryRequired) {
        this.deliveryRequired = deliveryRequired;
        this.email = email;
        this.deliveryAddr = address;
        this.tel = tel;
    }
}
