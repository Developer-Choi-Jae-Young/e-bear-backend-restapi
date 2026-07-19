package com.example.ebearrestapi.entity;

import com.example.ebearrestapi.etc.PgProvider;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class PgProviderEntity {
    @Id // pg사를 pk로 잡음
    @Column(name = "pg_code")
    @Enumerated(EnumType.STRING)
    private PgProvider pgCode; // 토스페이먼츠, NHN, KG이니시스 등

    @Column(name = "pg_name")
    private String pgName;

    @Column(name = "is_active")
    private boolean isActive;

    // 라우팅 룰과의 1:N 양방향 매핑 (필요에 따라 추가/생략 가능)
    @OneToMany(mappedBy = "pgProvider", cascade = CascadeType.ALL)
    private List<PgRoutingRuleEntity> routingRules = new ArrayList<>();
}
