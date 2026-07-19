package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.PgRoutingRuleEntity;
import com.example.ebearrestapi.etc.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PgRoutingRuleRepository extends JpaRepository<PgRoutingRuleEntity, Long> {
    /**
     * 사용자가 선택한 결제 수단(paymentType)과 매핑되는
     * 활성화된(isActive = true) PG사 라우팅 룰 리스트를 가져옴.
     */
    @Query("SELECT r FROM PgRoutingRuleEntity r " +
            "JOIN FETCH r.pgProvider p " +
            "WHERE r.paymentType = :paymentType " +
            "AND r.cardCompany = :cardCompany " +
            "AND p.isActive = true")
    List<PgRoutingRuleEntity> findAvailableRules(@Param("paymentType") PaymentType paymentType);
}
