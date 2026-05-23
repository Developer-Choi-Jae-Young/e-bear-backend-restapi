package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponRepository extends JpaRepository<CouponEntity, Long> {

    // 유저가 발급한 쿠폰 제외하고 조회
    @Query("""
        SELECT a FROM CouponEntity a
        WHERE a.couponQuantity > 0
        AND a.couponNo NOT IN (
            SELECT b.couponNo FROM CouponEntity b
            WHERE b.user.userNo = :userNo
        )
    """)
    List<CouponEntity> list(@Param("userNo") Long userNo);

    // 유저가 보유한 쿠폰 목록 조회
    List<CouponEntity> findByUser_UserNo(Long userNo);
}
