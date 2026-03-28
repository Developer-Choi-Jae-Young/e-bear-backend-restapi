package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<PointEntity,Integer> {

}
