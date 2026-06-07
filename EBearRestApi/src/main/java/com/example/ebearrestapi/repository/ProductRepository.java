package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity,Long> {
    @Query(
            value = "SELECT p FROM ProductEntity p " +
                    "JOIN FETCH p.user " +
                    "JOIN p.board b " +
                    "WHERE p.user = :user " +
                    "AND b.delYN = 'N' " +
                    "AND (" +
                    "  :kw IS NULL OR :kw = '' " +
                    "  OR (:type = 'all' AND (p.productName LIKE %:kw% OR CAST(p.productNo AS string) LIKE %:kw% OR p.user.userName LIKE %:kw%)) " +
                    "  OR (:type = 'name' AND p.productName LIKE %:kw%) " +
                    "  OR (:type = 'id' AND CAST(p.productNo AS string) LIKE %:kw%) " +
                    "  OR (:type = 'seller' AND p.user.userName LIKE %:kw%) " +
                    ")",
            countQuery = "SELECT count(p) FROM ProductEntity p " +
                    "JOIN p.board b " +
                    "WHERE p.user = :user " +
                    "AND b.delYN = 'N' " +
                    "AND (" +
                    "  :kw IS NULL OR :kw = '' " +
                    "  OR (:type = 'all' AND (p.productName LIKE %:kw% OR CAST(p.productNo AS string) LIKE %:kw% OR p.user.userName LIKE %:kw%)) " +
                    "  OR (:type = 'name' AND p.productName LIKE %:kw%) " +
                    "  OR (:type = 'id' AND CAST(p.productNo AS string) LIKE %:kw%) " +
                    "  OR (:type = 'seller' AND p.user.userName LIKE %:kw%) " +
                    ")"
    )
    Page<ProductEntity> searchWithFilterAdmin(@Param("user") UserEntity user,
                                         @Param("type") String type,
                                         @Param("kw") String kw,
                                         Pageable pageable);

    @Query(
            value = "SELECT p FROM ProductEntity p " +
                    "JOIN FETCH p.user " +
                    "JOIN p.board b " +
                    "WHERE b.delYN = 'N' " +
                    // 💡 수정됨: IN 절 사용 및 hasCategory 플래그 체크
                    "AND (:hasCategory = false OR p.category.categoryNo IN :categoryIds) " +
                    "AND (" +
                    "  :kw IS NULL OR :kw = '' " +
                    "  OR (:type = 'all' AND (p.productName LIKE CONCAT('%', :kw, '%') OR CAST(p.productNo AS string) LIKE CONCAT('%', :kw, '%') OR p.user.userName LIKE CONCAT('%', :kw, '%'))) " +
                    "  OR (:type = 'name' AND p.productName LIKE CONCAT('%', :kw, '%')) " +
                    "  OR (:type = 'id' AND CAST(p.productNo AS string) LIKE CONCAT('%', :kw, '%')) " +
                    "  OR (:type = 'seller' AND p.user.userName LIKE CONCAT('%', :kw, '%')) " +
                    ")",
            countQuery = "SELECT count(p) FROM ProductEntity p " +
                    "JOIN p.board b " +
                    "WHERE b.delYN = 'N' " +
                    "AND (:hasCategory = false OR p.category.categoryNo IN :categoryIds) " + // count 쿼리도 수정
                    "AND (" +
                    "  :kw IS NULL OR :kw = '' " +
                    "  OR (:type = 'all' AND (p.productName LIKE CONCAT('%', :kw, '%') OR CAST(p.productNo AS string) LIKE CONCAT('%', :kw, '%') OR p.user.userName LIKE CONCAT('%', :kw, '%'))) " +
                    "  OR (:type = 'name' AND p.productName LIKE CONCAT('%', :kw, '%')) " +
                    "  OR (:type = 'id' AND CAST(p.productNo AS string) LIKE CONCAT('%', :kw, '%')) " +
                    "  OR (:type = 'seller' AND p.user.userName LIKE CONCAT('%', :kw, '%')) " +
                    ")"
    )
    Page<ProductEntity> searchWithFilter(@Param("hasCategory") boolean hasCategory,
                                         @Param("categoryIds") List<Long> categoryIds,
                                         @Param("type") String type,
                                         @Param("kw") String kw,
                                         Pageable pageable);
}
