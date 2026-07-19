package com.example.ebearrestapi.service;

import com.example.ebearrestapi.entity.PgRoutingRuleEntity;
import com.example.ebearrestapi.etc.PaymentType;
import com.example.ebearrestapi.etc.PgProvider;
import com.example.ebearrestapi.repository.PgRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PgRoutingService {

    private final PgRoutingRuleRepository pgRoutingRuleRepository;

    /**
     * 사용자가 선택한 결제 정보(결제 수단, 카드사)를 기반으로
     * 수수료가 가장 낮고 활성화된 PG사를 찾아냅니다.
     */
    public PgProvider determineBestPg(PaymentType method, String cardCompany) {
        // PG사 활성화 상태면서 사용자가 선택한 결제 수단과 카드사 조건에 맞는 라우팅 룰 리스트 조회
        List<PgRoutingRuleEntity> activeRules = pgRoutingRuleRepository.findAvailableRules(method);

        if (activeRules.isEmpty()) {
            // 기본값 설정
            return PgProvider.TOSS;
        }

        PgRoutingRuleEntity bestRule = null;
        BigDecimal minFeeRate = BigDecimal.valueOf(99.0); // 수수료율 비교를 위한 최댓값 초기화

        // 수수료율(feeRate)이 가장 낮은 PG사 탐색
        for (PgRoutingRuleEntity rule : activeRules) {
            // PG사의 상태가 활성화되어 있는지 교차 확인
            if (rule.getPgProvider().isActive()) {
                if (rule.getFeeRate().compareTo(minFeeRate) < 0) {
                    minFeeRate = rule.getFeeRate();
                    bestRule = rule;
                }
            }
        }

        // 가장 수수료가 낮은 PG사 코드 반환
        return (bestRule != null) ? bestRule.getPgProvider().getPgCode() : PgProvider.TOSS;
    }
}
