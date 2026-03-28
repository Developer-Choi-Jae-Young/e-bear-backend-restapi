package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.CouponEntity;
import com.example.ebearrestapi.entity.PointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<CouponEntity,Integer> {

}
