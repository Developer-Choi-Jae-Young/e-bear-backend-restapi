package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.StateCode;
import com.example.ebearrestapi.exception.PaymentException;
import com.example.ebearrestapi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final OrderItemRepository orderItemRepository;
    private final StateCodeService stateCodeService;

    /**
     * 결제 승인 전 1차 사전 검증
     */
    @Transactional
    public PaymentEntity preValidate(PaymentConfirmDto confirmDto) {
        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(confirmDto.getOrderId())
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "주문 내역을 찾을 수 없습니다."));

        // 금액 검증 (위조 방지)
        if (!payment.getPaymentAmount().equals(confirmDto.getAmount())) {
            throw new PaymentException("FORGED_AMOUNT", "결제 금액이 조작되었습니다.");
        }

        return payment;
    }

    /**
     * 결제 승인 완료 이후 최종 상태 갱신 및 차감 처리
     */
    @Transactional
    public void completePayment(PaymentConfirmDto confirmDto, String paymentKey) {
        PaymentEntity payment = paymentRepository.findByOrderPayment_OrderPaymentId(confirmDto.getOrderId())
                .orElseThrow(() -> new PaymentException("NOT_FOUND", "주문 내역을 찾을 수 없습니다."));

        OrderPaymentEntity orderPayment = payment.getOrderPayment();
        Long userNo = orderPayment.getUser().getUserNo();

        // 포인트 사용 금액이 있을 경우에만 실행
        if (payment.getUsedPoint() != null && payment.getUsedPoint() > 0) {
            // 비관적 락 획득
            UserEntity user = userRepository.findByUserNoWithLock(userNo)
                    .orElseThrow(() -> new PaymentException("NOT_FOUND", "유저 정보를 찾을 수 없습니다."));

            // 현재 유저의 총 포인트 조회
            int currentTotalPoint = pointRepository.sumUseAmountByUserNo(userNo);

            // 잔액 검증
            if (currentTotalPoint < payment.getUsedPoint()) {
                throw new PaymentException("INSUFFICIENT_POINTS", "포인트가 부족하여 결제 승인을 완료할 수 없습니다.");
            }

            // 포인트 차감 내역 생성 및 INSERT
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
                    throw new PaymentException("COUPON_OWNER_MISMATCH", "본인 소유의 쿠폰이 아니므로 결제 승인을 완료할 수 없습니다.");
                }

                if (myCoupon.isUsed()) {
                    throw new PaymentException("COUPON_ALREADY_USED", "이미 사용 완료된 쿠폰이 포함되어 있어 결제 승인을 완료할 수 없습니다.");
                }

                myCoupon.use();
            }
        }

        // 최종 결제 객체 데이터 갱신
        payment.setPaymentStatus(PaymentStatus.DONE);
        payment.setPaymentKey(paymentKey);
        payment.setApprovedAt(LocalDateTime.now());
    }

    /**
     * 결제 취소/실패 처리
     */
    @Transactional
    public void failPayment(String orderId) {
        paymentRepository.findByOrderPayment_OrderPaymentId(orderId).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.ABORTED);
            paymentRepository.save(payment);
        });
    }
}
