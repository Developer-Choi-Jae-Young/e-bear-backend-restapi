package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.entity.PointEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointRepository extends JpaRepository<PointEntity,Integer> {

    // 유저의 모든 포인트 내역 합산 (null이면 0 반환)
    @Query("SELECT COALESCE(SUM(p.useAmount), 0) FROM PointEntity p WHERE p.user.userNo = :userNo")
    int sumUseAmountByUserNo(@Param("userNo") Long userNo);

}
