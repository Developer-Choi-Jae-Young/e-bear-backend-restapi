package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.OrderPaymentEntity;
import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {

    // 주문번호로 결제 내역 찾기 (검증 및 웹훅 처리 시 사용)
    Optional<PaymentEntity> findByOrderId(String orderId);

    Optional<PaymentEntity> findByOrderPayment(OrderPaymentEntity orderPayment);
}
