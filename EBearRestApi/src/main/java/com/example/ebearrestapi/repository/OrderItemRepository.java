package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.OrderItemEntity;
import com.example.ebearrestapi.entity.OrderPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderPayment(OrderPaymentEntity orderPayment);
    List<OrderItemEntity> findAllByOrderItemNo(Long orderItemNo);
}
