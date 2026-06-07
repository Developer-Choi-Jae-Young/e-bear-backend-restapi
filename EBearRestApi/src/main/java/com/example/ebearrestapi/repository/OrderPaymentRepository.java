package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.OrderPaymentEntity;
import com.example.ebearrestapi.etc.OrderStatus;
import com.example.ebearrestapi.etc.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderPaymentRepository extends JpaRepository<OrderPaymentEntity, String> {
    List<OrderPaymentEntity> findAllByOrderStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime before);
    List<OrderPaymentEntity> findAllByOrderStatus(OrderStatus status);

    // 15분 초과 OR 결제 실패(ABORTED, CANCELED, EXPIRED) 건만 필터링
    @Query("SELECT DISTINCT o FROM OrderPaymentEntity o " +
            "LEFT JOIN o.paymentList p " +
            "WHERE o.orderStatus = :orderStatus " +
            "AND (o.createdAt < :expirationTime " +
            "     OR p.paymentStatus IN :failedStatuses)")
    List<OrderPaymentEntity> findOrphanOrFailedOrders(
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("expirationTime") LocalDateTime expirationTime,
            @Param("failedStatuses") List<PaymentStatus> failedStatuses);
}
