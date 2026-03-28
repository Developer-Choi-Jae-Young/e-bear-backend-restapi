package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {

    Optional<PaymentEntity> findByOrderId(String orderId);

}
