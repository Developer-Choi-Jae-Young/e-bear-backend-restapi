package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.entity.OrderItemEntity;
import com.example.ebearrestapi.entity.OrderPaymentEntity;
import com.example.ebearrestapi.entity.PaymentEntity;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.repository.OrderItemRepository;
import com.example.ebearrestapi.repository.OrderPaymentRepository;
import com.example.ebearrestapi.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${toss.api.secret-key}")
    private String secretKey;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
//    private final UserRepository userRepository; // 포인트 차감을 위해 필요
    private final OrderPaymentRepository orderPaymentRepository; // 실제 가격 조회를 위해 필요
    private final OrderItemRepository orderItemRepository;

    public String readyPayment(PaymentDto paymentDto) {
        // 리액트에서 보낸 paymentAmount는 무시하고, DB에서 직접 계산
        // TODO: orderPaymentRepository.getOrderItems()를 순회하며 orderPaymentRepository에서 가격을 가져와 (가격*수량) 합산
        // 전달받은 주문 번호(PK)로 OrderPaymentEntity 조회
        Long orderPaymentId = Long.valueOf(paymentDto.getOrderId());
        OrderPaymentEntity orderPayment = orderPaymentRepository.findById(orderPaymentId)
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));
        // 해당 주문에 매핑된 주문 아이템(OrderItemEntity) 목록 조회
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(orderPayment);

        int totalProductPrice = 100; // DB 조회 결과 상품 총액이 100원이라고 가정
        for (OrderItemEntity item : orderItems) {
            // ProductOptionEntity에 가격(productOptionPrice)이 있다고 가정 (이전 OrderService 참고)
            int itemPrice = item.getProductOption().getProductOptionPrice();
            int quantity = item.getQuantity();

            totalProductPrice += (itemPrice * quantity);
        }

        // 쿠폰 및 포인트 검증(임시)
        // TODO: CouponRepository에서 paymentDto.getCouponId() 검증 및 할인액 계산
        // TODO: 쿠폰은 사용자가 가지고 있는 쿠폰과 넘어온 쿠폰이 같은지 검증 필요
        int couponDiscount = 0;
        int usePoint = paymentDto.getUsePoint() != null ? paymentDto.getUsePoint() : 0;

        // TODO: UserRepository에서 현재 보유 포인트가 usePoint보다 큰지 검증 로직 추가

        // 상품을 조회한 값으로 최종 결제 금액 산출
        //=> 변조 방지
        int safeFinalAmount = totalProductPrice - couponDiscount - usePoint;

        // 결제 정보 생성 (포인트 등은 임시)
        PaymentEntity payment = PaymentEntity.builder()
                .orderId(paymentDto.getOrderId())               //주문번호
                .paymentAmount(safeFinalAmount)                 //결제금액
                .paymentStatus(PaymentStatus.READY)             //결제상태
                .paymentType(paymentDto.getType())              //결제수단
                .usedPoint(usePoint)                            //임시 포인트 가격(나중에 사용자가 가지고 있는 포인트랑 검증 예정)
                .build();

        // 다른 사용자 상품 구매를 막기위해 재고 선점 로직 필요
        // TODO: 재영님 로직 - 묶어있는 상품 재고 -1 시키기

        // 결제 상태 ready인 결제 객체 저장
        PaymentEntity savePayment = paymentRepository.save(payment);

        return savePayment.getOrderId();
    }

    @Transactional
    public Object confirmPayment(PaymentConfirmDto paymentConfirmDto) {
        // DB 주문 찾기
        PaymentEntity payment = paymentRepository.findByOrderId(paymentConfirmDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("{\"code\":\"NOT_FOUND\", \"message\":\"주문 내역을 찾을 수 없습니다.\"}"));

        // 금액 검증 (위조 방지)
        if (!payment.getPaymentAmount().equals(paymentConfirmDto.getAmount())) {
            throw new RuntimeException("{\"code\":\"FORGED_AMOUNT\", \"message\":\"결제 금액이 조작되었습니다.\"}");
        }

        // 토스페이먼츠 승인 API 호출
        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 토스 서버로 보낼 JSON body 데이터
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("paymentKey", paymentConfirmDto.getPaymentKey());
        requestBody.put("orderId", paymentConfirmDto.getOrderId());
        requestBody.put("amount", paymentConfirmDto.getAmount());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // 정상 승인 시도 (이 부분에서 고객 통장에 돈 빠져나감)
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode tossResponse = objectMapper.readTree(response.getBody());
            String tossStatus = tossResponse.get("status").asText(); // "DONE" 또는 "WAITING_FOR_DEPOSIT"

            // TODO: UserEntity user = userRepository.findByIdWithLock(userId).orElseThrow(); (비관적 락 적용 조회)
            // if (user.getPoint() < payment.getUsedPoint()) {
            //     // 가지고 있는 포인트와 결제에 사용될 포인트가 높으면 예외처리(변조 가능성)  -> 토스 결제 취소(환불) API 호출 후 Exception 발생
            // }
            // user.deductPoint(payment.getUsedPoint()); // 진짜 포인트 차감
            // TODO: 쿠폰 상태를 '사용 완료'로 변경

            //  상태값에 따른 분기 처리
            if ("DONE".equals(tossStatus)) {
                // 신용카드, 간편결제 등 즉시 완료

                // TODO: 포인트/쿠폰 차감 로직 실행)

                //최종 결제 객체에 데이터 넣음
                payment.setPaymentStatus(PaymentStatus.DONE);
                payment.setPaymentKey(paymentConfirmDto.getPaymentKey());
                payment.setApprovedAt(LocalDateTime.now());

                return "success";

            } else {
                // 토스가 200 OK를 줬지만, 우리가 예상치 못한 이상한 상태값일 경우
                throw new RuntimeException("{\"code\":\"UNKNOWN_STATUS\", \"message\":\"알 수 없는 결제 상태입니다: " + tossStatus + "\"}");
            }

        } catch (HttpStatusCodeException e) {
            // 토스에서 에러(잔액부족 등)를 던졌을 때 실패 상태로 저장
            payment.setPaymentStatus(PaymentStatus.ABORTED);
            paymentRepository.save(payment);
            // 토스가 준 에러 JSON(code, message 포함)을 그대로 던짐
            throw new IllegalArgumentException(e.getResponseBodyAsString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("{\"code\":\"JSON_PARSE_ERROR\", \"message\":\"결제 서버의 응답 데이터를 해석하는 중 오류가 발생했습니다.\"}");
        }
    }

    public Map<String, Object> getPaymentDetails(String orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        Map<String, Object> details = new HashMap<>();
        details.put("status", payment.getPaymentStatus().name());
        details.put("totalAmount", payment.getPaymentAmount());

        // TODO: 실제 상품 리스트를 DB에서 조회하여 반환해야 함
        details.put("products", new ArrayList<>());

        return details;
    }

    @Transactional
    public void handleAbortedWebhook(String orderId) {
        // orderId로 주문 내역 찾음
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // 현재 상태가 READY인 경우에만 ABORTED로 변경
        // 이미 DONE으로 끝난 정상 결제인데 지연된 웹훅이 와서 덮어씌우는 것 방지
        if (payment.getPaymentStatus() == PaymentStatus.READY) {
            payment.setPaymentStatus(PaymentStatus.ABORTED);

            // TODO: 롤백 부분으로 여기서 미리 빼두었던 상품 재고를 다시 +1 해주는 로직 실행
        }
    }
}
