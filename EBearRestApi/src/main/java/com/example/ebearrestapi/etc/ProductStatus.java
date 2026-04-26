package com.example.ebearrestapi.etc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductStatus {
    SALE("판매중"),
    SOLD_OUT("품절"),
    HIDDEN("숨김"),
    STOP("판매중지");

    private final String name;
}
