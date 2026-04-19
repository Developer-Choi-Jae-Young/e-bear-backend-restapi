package com.example.ebearrestapi.dto.request;
import lombok.Data;

@Data
public class TossWebhookDto {

    // 웹훅이 날아왔는지 알려주는 타입
    // ex: "PAYMENT_STATUS_CHANGED" (결제 상태 변경)
    private String eventType;

    // 알림 생성 시간
    // ex: "2026-04-04T12:30:00+09:00"
    private String createdAt;

    // 실제 결제 정보 객체
    private WebhookData data;

    @Data
    public static class WebhookData {
        // DB에서 주문을 찾기 위한 식별자
        private String orderId;

        // 토스 측의 결제 고유 식별자 (환불이나 조회 시 필요)
        private String paymentKey;

        // 현재 변경된 최종 결제 상태
        // ex: "DONE"(성공), "CANCELED"(취소), "ABORTED"(실패/중단)
        private String status;

        // 사용자가 결제한 총 금액
        private Integer totalAmount;

        // 결제 수단 (ex: "카드", "가상계좌" 등 - 필요에 따라 추가)
        private String method;
    }
}