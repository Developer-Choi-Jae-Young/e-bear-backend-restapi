package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    // 특정 회원의 장바구니 목록 전체 조회
    List<CartEntity> findByUser_UserNo(Long userNo);

    // 동일 회원, 동일 상품, 동일 옵션의 장바구니 아이템 단건 조회 (중복 담기 방지용)
    @Query("SELECT c FROM CartEntity c " +
            "WHERE c.user.userNo = :userNo " +
//            "AND c.product.productNo = :productNo " +
            "AND c.productOption.productOptionNo = :productOptionNo")
    Optional<CartEntity> findCartItem(
            @Param("userNo") Long userNo,
            @Param("productNo") Long productNo,
            @Param("productOptionNo") Long productOptionNo
    );
}
