package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.OrderListEntity;
import com.example.ebearrestapi.entity.PaymentEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderListRepository extends JpaRepository<OrderListEntity,Integer> {

//    @Query("select o from Order o join fetch o.orderItems where o.orderListNo = :orderListNo")
//    Optional<Object> findByOrderListNoWithProduct(@Param("orderListNo") Long orderListNo);
}
