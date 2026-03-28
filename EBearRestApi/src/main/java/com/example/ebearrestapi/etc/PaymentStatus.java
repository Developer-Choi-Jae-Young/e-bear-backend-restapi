package com.example.ebearrestapi.etc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 생성"),              // 결제 요청이 생성됨
    IN_PROGRESS("결제 인증 중"),      // 사용자가 결제창에서 인증 중
    WAITING_FOR_DEPOSIT("입금 대기"), // 가상계좌 입금 대기
    DONE("결제 완료"),               // 결제 승인이 성공적으로 완료됨
    CANCELED("결제 취소"),           // 사용자가 결제를 취소하거나 서버에서 취소함
    ABORTED("결제 실패"),            // 결제 승인이 실패함
    EXPIRED("기간 만료");            // 결제 유효 시간이 지남

    private final String description;
}
