package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.PaymentConfirmDto;
import com.example.ebearrestapi.dto.request.PaymentDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.PaymentStatus;
import com.example.ebearrestapi.etc.PaymentType;
import com.example.ebearrestapi.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private UserRepository userRepository;
    @Mock private OrderPaymentRepository orderPaymentRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PointRepository pointRepository;
    @Mock private StateCodeService stateCodeService;

    private UserEntity mockUser;
    private OrderPaymentEntity mockOrderPayment;
    private PaymentEntity mockPayment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "secretKey", "test_secret_key");

        mockUser = UserEntity.builder()
                .userNo(1L)
                .build();

        mockOrderPayment = OrderPaymentEntity.builder()
                .orderPaymentId("1")
                .user(mockUser)
                .paymentList(new ArrayList<>())
                .build();

        mockPayment = PaymentEntity.builder()
                .paymentNo(1L)
                .paymentAmount(10000)
                .paymentStatus(PaymentStatus.READY)
                .paymentType(PaymentType.CARD)
                .orderPayment(mockOrderPayment)
                .usedPoint(0)
                .build();
    }

    @Test
    @DisplayName("readyPayment 성공 케이스: 결제 정보 생성 및 저장")
    void readyPayment_Success() {
        // given
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrderPaymentId("1");
        paymentDto.setType(PaymentType.CARD);
        paymentDto.setUsePoint(1000);

        ProductOptionEntity option = ProductOptionEntity.builder()
                .productOptionPrice(5000) //하나의 5000원
                .build();
        OrderItemEntity item = OrderItemEntity.builder()
                .productOption(option)
                .quantity(2) // 개수가 2개
                .build();

        when(orderPaymentRepository.findById("1")).thenReturn(Optional.of(mockOrderPayment));
        when(orderItemRepository.findByOrderPayment(mockOrderPayment)).thenReturn(List.of(item));

        // when
        paymentService.readyPayment(paymentDto);

        // then
        // 물품 가격(5000*2) - 포인트(1000) = 결재금액(9100)
        verify(paymentRepository, times(1)).save(argThat(payment -> 
            payment.getPaymentAmount() == 9100 &&
            payment.getPaymentStatus() == PaymentStatus.READY &&
            payment.getPaymentType() == PaymentType.CARD &&
            payment.getUsedPoint() == 1000
        ));

        assertEquals(1, mockOrderPayment.getPaymentList().size());
    }

    @Test
    @DisplayName("readyPayment 실패 케이스: 주문 정보 없음")
    void readyPayment_Fail_OrderNotFound() {
        // given
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setOrderPaymentId("99"); //이런 주문id 없음
        when(orderPaymentRepository.findById("99")).thenReturn(Optional.empty());

        // when & then
        // 없으니 runtime 에러 나는지 확인
        assertThrows(RuntimeException.class, () -> paymentService.readyPayment(paymentDto));
    }

    @Test
    @DisplayName("confirmPayment 성공 케이스: 일반 결제")
    void confirmPayment_Success() throws JsonProcessingException {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(10000);
        dto.setPaymentKey("toss_key_123");

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));

        String tossResponseJson = "{\"status\": \"DONE\"}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(tossResponseJson, HttpStatus.OK));

        when(orderItemRepository.findByOrderPayment(any())).thenReturn(new ArrayList<>());

        // when
        Object result = paymentService.confirmPayment(dto);

        // then
        assertEquals("success", result);
        assertEquals(PaymentStatus.DONE, mockPayment.getPaymentStatus());
        assertEquals("toss_key_123", mockPayment.getPaymentKey());
        assertNotNull(mockPayment.getApprovedAt());
    }

    @Test
    @DisplayName("confirmPayment 실패 케이스: 금액 조작")
    void confirmPayment_Fail_ForgedAmount() {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(5000); // 임시엔 10000원임

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(dto));
        assertTrue(ex.getMessage().contains("FORGED_AMOUNT"));
    }

    @Test
    @DisplayName("confirmPayment 실패 케이스: 포인트 잔액 부족 및 토스 결제 취소")
    void confirmPayment_Fail_LackOfPoint() {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(10000);
        dto.setPaymentKey("toss_key_123");

        mockPayment.setUsedPoint(5000);

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));
        when(restTemplate.postForEntity(contains("/confirm"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\": \"DONE\"}", HttpStatus.OK));

        when(userRepository.findByUserNoWithLock(1L)).thenReturn(Optional.of(mockUser));
        when(pointRepository.sumUseAmountByUserNo(1L)).thenReturn(3000); // 임시유저엔 3000포인트 줌

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(dto));
        assertTrue(ex.getMessage().contains("포인트가 부족하여"));
        
        // 취소 API 호출 확인
        verify(restTemplate, times(1)).postForEntity(contains("/cancel"), any(), eq(String.class));
    }

    @Test
    @DisplayName("confirmPayment 실패 케이스: 남의 쿠폰 사용 시도")
    void confirmPayment_Fail_InvalidCouponOwner() {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(10000);
        dto.setPaymentKey("toss_key_123");

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));
        when(restTemplate.postForEntity(contains("/confirm"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\": \"DONE\"}", HttpStatus.OK));

        UserEntity otherUser = UserEntity.builder().userNo(99L).build();
        MyCouponEntity coupon = MyCouponEntity.builder().user(otherUser).isUsed(false).build();
        OrderItemEntity item = OrderItemEntity.builder().myCoupon(coupon).build();

        when(orderItemRepository.findByOrderPayment(any())).thenReturn(List.of(item));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(dto));
        assertTrue(ex.getMessage().contains("본인 소유의 쿠폰이 아니므로"));
        verify(restTemplate, times(1)).postForEntity(contains("/cancel"), any(), eq(String.class));
    }

    @Test
    @DisplayName("confirmPayment 실패 케이스: 이미 사용된 쿠폰")
    void confirmPayment_Fail_CouponAlreadyUsed() {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(10000);
        dto.setPaymentKey("toss_key_123");

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));
        when(restTemplate.postForEntity(contains("/confirm"), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"status\": \"DONE\"}", HttpStatus.OK));

        MyCouponEntity coupon = MyCouponEntity.builder().user(mockUser).isUsed(true).build();
        OrderItemEntity item = OrderItemEntity.builder().myCoupon(coupon).build();

        when(orderItemRepository.findByOrderPayment(any())).thenReturn(List.of(item));

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.confirmPayment(dto));
        assertTrue(ex.getMessage().contains("이미 사용 완료된 쿠폰"));
        verify(restTemplate, times(1)).postForEntity(contains("/cancel"), any(), eq(String.class));
    }

    @Test
    @DisplayName("confirmPayment 실패 케이스: 토스 API 에러 (HttpStatusCodeException)")
    void confirmPayment_Fail_TossApiError() {
        // given
        PaymentConfirmDto dto = new PaymentConfirmDto();
        dto.setOrderId("1");
        dto.setAmount(10000);

        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid Key", "{\"message\":\"error\"}".getBytes(), null));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> paymentService.confirmPayment(dto));
        assertEquals(PaymentStatus.ABORTED, mockPayment.getPaymentStatus());
    }

    @Test
    @DisplayName("handleAbortedWebhook 성공 케이스: READY 상태인 경우 재고 롤백")
    void handleAbortedWebhook_Success_Rollback() {
        // given
        mockPayment.setPaymentStatus(PaymentStatus.READY);
        when(paymentRepository.findByOrderPayment_OrderPaymentId("1")).thenReturn(Optional.of(mockPayment));

        ProductOptionEntity option = mock(ProductOptionEntity.class);
        OrderItemEntity item = OrderItemEntity.builder().productOption(option).quantity(5).build();
        when(orderItemRepository.findByOrderPayment(any())).thenReturn(List.of(item));

        // when
        paymentService.handleAbortedWebhook("1");

        // then
        assertEquals(PaymentStatus.ABORTED, mockPayment.getPaymentStatus());
        verify(option, times(1)).increaseProductOptionQuantity(5);
    }

}
