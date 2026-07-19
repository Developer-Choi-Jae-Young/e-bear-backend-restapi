package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.CartEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    // 특정 회원의 장바구니 목록 전체 조회
    List<CartEntity> findByUser(UserEntity user);

    // 동일 회원, 동일 상품, 동일 옵션의 장바구니 아이템 단건 조회 (중복 담기 방지용)
    @Query("""
        SELECT a FROM CartEntity a
        WHERE a.user.userNo = :userNo
        AND a.productOption.productOptionNo = :productOptionNo
    """)
    Optional<CartEntity> findCartItem(
        @Param("userNo") Long userNo,
        @Param("productOptionNo") Long productOptionNo
    );
}
