package com.example.ebearrestapi.etc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
    // 1. 주요 결제 수단 (Method)
    CARD("카드"),
    TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰"),

    // 2. 상품권 및 기타 수단
//    CULTURE_GIFT_CERTIFICATE("문화상품권"),
//    BOOK_GIFT_CERTIFICATE("도서문화상품권"),
//    GAME_GIFT_CERTIFICATE("게임문화상품권"),

    // 3. 간편결제 전용 (선택 사항)
    TOSS_PAY("토스페이"),
    NAVER_PAY("네이버페이"),
    KAKAO_PAY("카카오페이");

    // 4. 결제 유형 (Category)
//    NORMAL("일반결제"),
//    BILLING("정기결제"),
//    BRANDPAY("브랜드페이");

    private final String description;

    // 문자열 값을 바탕으로 Enum을 찾아주는 편의 메서드
    public static PaymentType fromString(String text) {
        for (PaymentType type : PaymentType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}
