package com.example.ebearrestapi.etc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StateCode {
    CHARGE(100L, "충전"),
    DEDUCTED(101L, "차감"),
    TEST(102L, "테스트");

    private final Long value;
    private final String name;
}
