package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.StateCode;
import com.example.ebearrestapi.repository.*;
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
    private final UserRepository userRepository; // 포인트 차감을 위해 필요
    private final OrderPaymentRepository orderPaymentRepository; // 실제 가격 조회를 위해 필요
    private final OrderItemRepository orderItemRepository;
    private final PointRepository pointRepository;
    private final StateCodeService stateCodeService;

    public void readyPayment(PaymentDto paymentDto) {
        String opId = paymentDto.getOrderPaymentId();

        // 리액트에서 보낸 paymentAmount는 무시하고, DB에서 직접 계산
        // TODO: orderPaymentRepository.getOrderItems()를 순회하며 orderPaymentRepository에서 가격을 가져와 (가격*수량) 합산
        // 주문 정보 조회
        OrderPaymentEntity orderPayment = orderPaymentRepository.findById(opId)
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));
        // 해당 주문에 매핑된 주문 아이템(OrderItemEntity) 목록 조회
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(orderPayment);

        // 결제 금액 계산
        int totalProductPrice = 0; // DB 조회 결과 상품 총액이 100원이라고 가정
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
                .paymentAmount(safeFinalAmount)                 //결제금액
                .paymentStatus(PaymentStatus.READY)             //결제상태
                .paymentType(paymentDto.getType())              //결제수단
                .usedPoint(usePoint)                            //임시 포인트 가격(나중에 사용자가 가지고 있는 포인트랑 검증 예정)
                // .usedCouponId(null)                          // TODO: 쿠폰 ID 세팅
                .orderPayment(orderPayment)                     // 연관된 주문 초기화
                .build();

        // 양방향 연관관계 매핑
        orderPayment.getPaymentList().add(payment);

        // 결제 상태 ready인 결제 객체 저장
        paymentRepository.save(payment);

    }

    @Transactional
    public Object confirmPayment(PaymentConfirmDto paymentConfirmDto) {
        String opId = paymentConfirmDto.getOrderId();

        // DB 주문 찾기
        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(opId)
                .orElseThrow(() -> new RuntimeException("{\"code\":\"NOT_FOUND\", \"message\":\"주문 내역을 찾을 수 없습니다.\"}"));

        // 금액 검증 (위조 방지)
        if (!payment.getPaymentAmount().equals(paymentConfirmDto.getAmount())) {
            throw new RuntimeException("{\"code\":\"FORGED_AMOUNT\", \"message\":\"결제 금액이 조작되었습니다.\"}");
        }

        // 토스페이먼츠 승인 API 호출
        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuthKey);
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

            // 상태값에 따른 분기 처리
            // 신용카드, 간편결제 등 즉시 완료
            if ("DONE".equals(tossStatus)) {
                OrderPaymentEntity orderPayment = payment.getOrderPayment();
                Long userNo = orderPayment.getUser().getUserNo();

                // 포인트 사용 금액이 있을 경우에만 실행
                if (payment.getUsedPoint() != null && payment.getUsedPoint() > 0) {
                    // 토스 통신이 끝난 상태에서 비관적 락 획득
                    UserEntity user = userRepository.findByUserNoWithLock(userNo)
                            .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

                    // 현재 유저의 총 포인트 조회 (따닥 시 위 로직에서 대기 중)
                    int currentTotalPoint = pointRepository.sumUseAmountByUserNo(userNo);

                    // 잔액 검증
                    if (currentTotalPoint < payment.getUsedPoint()) {
                        cancelTossPayment(paymentConfirmDto.getPaymentKey(), "포인트 잔액 부족");
                        throw new RuntimeException("포인트가 부족하여 결제가 자동 취소되었습니다.");
                    }

                    // 포인트 차감 내역(PointEntity) 생성 및 INSERT
                    PointEntity deductPoint = PointEntity.builder()
                            .useAmount(-payment.getUsedPoint()) // 사용 금액을 마이너스로 기록
                            .user(user)
                            .stateCode(stateCodeService.findByStateCodeNo(StateCode.DEDUCTED))
                            .build();
                    pointRepository.save(deductPoint);
                }

                // 쿠폰 상태를 '사용 완료'로 변경
                List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(orderPayment);
                for (OrderItemEntity item : orderItems) {
                    MyCouponEntity myCoupon = item.getMyCoupon();

                    if (myCoupon != null) {
                        if (!myCoupon.getUser().getUserNo().equals(userNo)) {
                            cancelTossPayment(paymentConfirmDto.getPaymentKey(), "쿠폰 소유자 불일치");
                            throw new RuntimeException("본인 소유의 쿠폰이 아니므로 결제가 자동 취소되었습니다.");
                        }

                        if (myCoupon.isUsed()) {
                            cancelTossPayment(paymentConfirmDto.getPaymentKey(), "이미 사용된 쿠폰");
                            throw new RuntimeException("이미 사용 완료된 쿠폰이 포함되어 있어 결제가 취소되었습니다.");
                        }

                        myCoupon.use();
                    }
                }

                //최종 결제 객체에 데이터 넣음
                payment.setPaymentStatus(PaymentStatus.DONE);
                payment.setPaymentKey(paymentConfirmDto.getPaymentKey());
                payment.setApprovedAt(LocalDateTime.now());

                return "success";

            } else {
                // 토스가 "DONE"이 아닌 다른 상태값을 줬을 떄
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

    // 결제 취소
    private void cancelTossPayment(String paymentKey, String cancelReason) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

        String encodedAuthKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cancelReason", cancelReason);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, requestEntity, String.class);
            System.out.println("토스페이먼츠 결제 취소 완료: " + paymentKey);
        } catch (Exception e) {
            System.err.println("치명적 에러: 결제 취소 실패! 수동 환불 필요: " + paymentKey);
        }
    }

    public Map<String, Object> getPaymentDetails(String orderPaymentId) {
        String opId = orderPaymentId;

        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(opId)
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
        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문을 찾을 수 없습니다."));

        // 현재 상태가 READY인 경우에만 ABORTED로 변경
        // 이미 DONE으로 끝난 정상 결제인데 지연된 웹훅이 와서 덮어씌우는 것 방지(중복 처리 방지)
        if (payment.getPaymentStatus() == PaymentStatus.READY) {
            payment.setPaymentStatus(PaymentStatus.ABORTED);

            // 상품 재고를 롤백(차감) 로직 실행
            OrderPaymentEntity orderPayment = payment.getOrderPayment();
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(orderPayment);

            for (OrderItemEntity item : orderItems) {
                ProductOptionEntity productOption = item.getProductOption();
                int rollbackQuantity = item.getQuantity();

                // 차감했던 수량만큼 다시 더해줌
                productOption.increaseProductOptionQuantity(rollbackQuantity);
            }
        }
    }
}
