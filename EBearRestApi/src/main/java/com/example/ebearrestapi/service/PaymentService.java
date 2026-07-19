package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.dto.response.PaymentDetailsDto;
import com.example.ebearrestapi.dto.response.PaymentProductDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.PgProvider;
import com.example.ebearrestapi.exception.PaymentException;
import com.example.ebearrestapi.gateway.PaymentGateway;
import com.example.ebearrestapi.gateway.PaymentGatewayRegistry;
import com.example.ebearrestapi.gateway.dto.PaymentResponseDto;
import com.example.ebearrestapi.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderPaymentRepository orderPaymentRepository; // 실제 가격 조회를 위해 필요
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final PgRoutingService pgRoutingService;
    private final PaymentGatewayRegistry paymentGatewayRegistry;

    @Transactional
    public void readyPayment(PaymentDto paymentDto) {
        String opId = paymentDto.getOrderPaymentId();

        // 리액트에서 보낸 paymentAmount는 무시하고, DB에서 직접 계산
        // TODO: orderPaymentRepository.getOrderItems()를 순회하며 orderPaymentRepository에서 가격을 가져와 (가격*수량) 합산
        // 주문 정보 조회
        OrderPaymentEntity orderPayment = orderPaymentRepository.findById(opId)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "주문 정보를 찾을 수 없습니다."));
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

        PgProvider determinedPg = pgRoutingService.determineBestPg(
                paymentDto.getType(),   // 사용자가 고른 결제수단 (CARD, TRANSFER 등)
                "ALL"                   // 특정 카드사 구분 없이
        );

        // 결제 정보 생성 (포인트 등은 임시)
        PaymentEntity payment = PaymentEntity.builder()
                .paymentAmount(safeFinalAmount)                 //결제금액
                .paymentStatus(PaymentStatus.READY)             //결제상태
                .paymentType(paymentDto.getType())              //결제수단
                .pgProvider(determinedPg)                       //PG사 결정
                .usedPoint(usePoint)                            //임시 포인트 가격(나중에 사용자가 가지고 있는 포인트랑 검증 예정)
                // .usedCouponId(null)                          // TODO: 쿠폰 ID 세팅
                .orderPayment(orderPayment)                     // 연관된 주문 초기화
                .build();

        // 양방향 연관관계 매핑
        orderPayment.getPaymentList().add(payment);

        // 결제 상태 ready인 결제 객체 저장
        paymentRepository.save(payment);

    }

    public void confirmPayment(PaymentConfirmDto paymentConfirmDto) {
        // 사전 검증 (DB 조회 트랜잭션 분리)
        PaymentEntity payment = paymentTransactionService.preValidate(paymentConfirmDto);
        // PG 게이트웨이 결정
        PgProvider pgProvider = paymentConfirmDto.getPgProvider();
        // 해당 PG사에 알맞은 어댑터 획득
        PaymentGateway gateway = paymentGatewayRegistry.getGateway(pgProvider);
        PaymentResponseDto gatewayResponse;

        try {
            // 해당 PG사 서버로 외부 API 호출 위임
            gatewayResponse = gateway.confirm(paymentConfirmDto);
        } catch (RestClientException e) {
            // 타임아웃 / 네트워크 장애 등
            log.error("PG confirm API network timeout! orderId: {}", paymentConfirmDto.getOrderId(), e);
            try {
                // 망 취소(Network Cancel) 호출
                gateway.cancel(paymentConfirmDto.getPaymentKey(), "네트워크 타임아웃으로 인한 자동 취소");
            } catch (Exception cancelEx) {
                log.error("CRITICAL ERROR: Failed to cancel toss payment after confirm API timeout! orderId: {}", paymentConfirmDto.getOrderId(), cancelEx);
            }
            paymentTransactionService.failPayment(paymentConfirmDto.getOrderId());
            throw new PaymentException("NETWORK_ERROR", "결제 서버와의 네트워크 오류로 인해 결제가 취소되었습니다.");
        }

        // PG사 결과 공통 검증
        if (gatewayResponse == null || !gatewayResponse.isSuccess()) {
            paymentTransactionService.failPayment(paymentConfirmDto.getOrderId());
            String errorMsg = (gatewayResponse != null) ? gatewayResponse.getErrorMessage() : "알 수 없는 승인 실패";
            throw new PaymentException("UNKNOWN_PAYMENT_STATUS", "결제가 승인되지 않았습니다. 사유: " + errorMsg);
        }

        // 결제 완료 처리 및 포인트/쿠폰 차감
        try {
            paymentTransactionService.completePayment(paymentConfirmDto, paymentConfirmDto.getPaymentKey());
        } catch (Exception e) {
            // 완료 처리 도중 예외 발생 시 결제 취소 API 호출
            try {
                gateway.cancel(gatewayResponse.getTransactionId(), e.getMessage());
                paymentTransactionService.failPayment(paymentConfirmDto.getOrderId());
            } catch (Exception cancelEx) {
                // 결제 취소 API마저 실패한 경우
                 log.error("CRITICAL ERROR: Toss Cancel API failed after DB Complete Error! orderId: " + paymentConfirmDto.getOrderId(), cancelEx);
            }
            throw e;
        }

    }

    @Transactional(readOnly = true)
    public PaymentDetailsDto getPaymentDetails(String orderPaymentId) {
        String opId = orderPaymentId;

        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(opId)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "주문 정보를 찾을 수 없습니다."));

        // 주문에 속한 상품 리스트(OrderItemEntity) 조회 및 DTO 변환
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(payment.getOrderPayment());

        List<PaymentProductDto> productDtos = orderItems.stream()
                .map(item -> PaymentProductDto.builder()
                        .productName(item.getProductOption().getProduct().getProductName())
                        .optionName(item.getProductOption().getProductOptionName())
                        .quantity(item.getQuantity())
                        .price(item.getProductOption().getProductOptionPrice())
                        .build())
                .toList();

        return PaymentDetailsDto.builder()
                .orderPaymentId(payment.getOrderPayment().getOrderPaymentId())
                .paymentStatus(payment.getPaymentStatus().name())
                .totalAmount(payment.getPaymentAmount())
                .usedPoint(payment.getUsedPoint())
                .approvedAt(payment.getApprovedAt())
                .products(productDtos)
                .build();
    }

    @Transactional
    public void handleAbortedWebhook(String orderId) {
        // orderId로 주문 내역 찾음
        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(orderId)
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "주문 정보를 찾을 수 없습니다."));

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
