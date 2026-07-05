package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.OrderItemEntity;
import com.example.ebearrestapi.entity.OrderPaymentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    @EntityGraph(attributePaths = {"productOption", "productOption.product"})
    List<OrderItemEntity> findByOrderPayment(OrderPaymentEntity orderPayment);

    @EntityGraph(attributePaths = {"productOption", "productOption.product"})
    List<OrderItemEntity> findAllByOrderItemNo(Long orderItemNo);
}
