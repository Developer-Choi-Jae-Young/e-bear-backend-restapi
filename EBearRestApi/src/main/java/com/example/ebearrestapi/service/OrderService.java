package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.OrderDto;
import com.example.ebearrestapi.dto.request.OrderSaveReqDto;
import com.example.ebearrestapi.dto.response.*;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.OrderStatus;
import com.example.ebearrestapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductOptionRepository productOptionRepository;
    private final MyCouponRepository myCouponRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderSaveResultDto saveOrder(OrderSaveReqDto orderDto, User user) {
        Long generatedOrderItemNo = createNewOrderItemNo();
        UserEntity newUser = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        List<OrderItemEntity> savedOrderItems = orderDto.getProductOptionList().stream().map(productOption -> {
            int quantity = productOption.getOptionCount();
            ProductOptionEntity productOptionEntity = productOptionRepository.findById(productOption.getProductOptionId()).orElseThrow(() -> new RuntimeException("Product Option Not Found"));

            return orderItemRepository.save(OrderItemEntity.builder()
                    .orderItemNo(generatedOrderItemNo)
                    .orderPayment(null)
                    .productOption(productOptionEntity)
                    .user(newUser)
                    .quantity(quantity).build());
        }).toList();

        if (savedOrderItems.isEmpty()) {
            throw new RuntimeException("저장할 주문 상품이 없습니다.");
        }

        long distinctCount = savedOrderItems.stream()
                .map(OrderItemEntity::getOrderItemNo)
                .distinct()
                .count();

        if (distinctCount > 1) {
            throw new RuntimeException("주문 상품들의 orderItemNo가 일치하지 않습니다.");
        }

        Long validOrderItemNo = savedOrderItems.get(0).getOrderItemNo();

        return OrderSaveResultDto.builder()
                .orderItemId(validOrderItemNo)
                .build();
    }

    @Transactional
    public OrderResultDto updateOrder(OrderDto orderDto, User user) {
        UserEntity newUser = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        OrderPaymentEntity newOrderPayment = orderPaymentRepository.save(OrderPaymentEntity.builder()
                .orderPaymentId(UUID.randomUUID().toString())
                .orderStatus(OrderStatus.PAYMENT_WAIT)
                .deliveryAddr(orderDto.getAddress())
                .tel(orderDto.getTel())
                .email(orderDto.getEmail())
                .user(newUser)
                .deliveryRequired(orderDto.getDeliveryRequired())
                .build());

        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrderItemNo(orderDto.getOrderId());

        if (orderItems.isEmpty()) {
            throw new RuntimeException("주문 상품을 찾을 수 없습니다.");
        }

        for (OrderItemEntity item : orderItems) {
            item.setOrderPayment(newOrderPayment);

            ProductOptionEntity productOption = item.getProductOption();
            productOption.decreaseProductOptionQuantity(item.getQuantity());
        }

        return OrderResultDto.builder()
                .orderPaymentId(newOrderPayment.getOrderPaymentId())
                .build();
    }

    public OrderSelectResultDto selectOrder(Long orderItemNo) {
        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrderItemNo(orderItemNo);

        if (orderItems.isEmpty()) {
            throw new RuntimeException("해당 주문번호의 상품을 찾을 수 없습니다.");
        }

        List<ProductOptionResultDto> productOptionResultDtoList = orderItems.stream()
                .map(orderItemEntity -> {
                    ProductOptionEntity productOption = orderItemEntity.getProductOption();

                    return ProductOptionResultDto.builder()
                            .productOptionId(productOption.getProductOptionNo())
                            .productOptionName(productOption.getProductOptionName())
                            .productName(productOption.getProduct().getProductName())
                            .quantity(orderItemEntity.getQuantity())
                            .price(productOption.getProductOptionPrice())
                            .couponNo(orderItemEntity.getMyCoupon() != null ? orderItemEntity.getMyCoupon().getMyCouponId() : null)
                            .seller(orderItemEntity.getUser().getUserNo())
                            .build();
                })
                .toList();

        OrderPaymentEntity orderPaymentEntity = orderItems.get(0).getOrderPayment();

        String address = null;
        String phone = null;
        String email = null;
        String deliveryRequired = null;
        OrderStatus orderStatus = null;

        if (orderPaymentEntity != null) {
             address = orderPaymentEntity.getDeliveryAddr();
             phone = orderPaymentEntity.getTel();
             email = orderPaymentEntity.getEmail();
             deliveryRequired = orderPaymentEntity.getDeliveryRequired();
             orderStatus = orderPaymentEntity.getOrderStatus();
        }

        return OrderSelectResultDto.builder()
                .orderItemId(orderItemNo)
                .address(address)
                .phone(phone)
                .email(email)
                .deliveryRequired(deliveryRequired)
                .orderStatus(orderStatus)
                .point(0)
                .productOptions(productOptionResultDtoList)
                .build();
    }

    public List<OrderSelectListResultDto> selectList(Pageable pageable) {
        Page<OrderPaymentEntity> orderPayments = orderPaymentRepository.findAll(pageable);

        return orderPayments.stream().map(orderPayment -> {
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderPayment(orderPayment);
            List<ProductOptionDto> productOptionDtos = orderItems.stream().map(item -> {
                Long reviewId = reviewRepository.findByProduct(item.getProductOption().getProduct())
                        .stream().map(ReviewEntity::getReviewNo).findFirst().orElse(null);

                return ProductOptionDto.builder()
                        .productOptionId(item.getProductOption().getProductOptionNo())
                        .productOptionName(item.getProductOption().getProductOptionName())
                        .quantity(item.getQuantity())
                        .couponNo(item.getMyCoupon() != null ? item.getMyCoupon().getMyCouponId() : 0)
                        .price(item.getProductOption().getProductOptionPrice())
                        .reviewId(reviewId)
                        .build();
            }).toList();

            String representativeName = orderItems.isEmpty() ? "상품 정보 없음" : orderItems.get(0).getProductOption().getProduct().getProductName();
            PaymentEntity paymentEntity = paymentRepository.findByOrderPayment(orderPayment).orElseThrow(() -> new RuntimeException("OrderPayment not found"));

            return OrderSelectListResultDto.builder()
                    .paymentId(paymentEntity.getPaymentNo())
                    .orderPaymentId(orderPayment.getOrderPaymentId())
                    .orderStatus(orderPayment.getOrderStatus())
                    .productOptions(productOptionDtos)
                    .productName(orderItems.size() > 1 ? representativeName + " 외 " + (orderItems.size() - 1) + "건" : representativeName)
                    .orderDate(orderPayment.getCreatedAt().toLocalDate())
                    .receiver(orderPayment.getUser().getUserName()).build();
        }).toList();
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupOrphanOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        List<OrderPaymentEntity> expiredOrders = orderPaymentRepository
                .findAllByOrderStatusAndCreatedAtBefore(OrderStatus.PAYMENT_WAIT, expirationTime);

        if (expiredOrders.isEmpty()) return;

        for (OrderPaymentEntity order : expiredOrders) {
            orderItemRepository.findByOrderPayment(order).forEach(orderItem -> {
                ProductOptionEntity option = orderItem.getProductOption();
                option.increaseProductOptionQuantity(orderItem.getQuantity());
            });

            order.setOrderStatus(OrderStatus.CANCEL);
            order.setDelYn(true);
        }
    }

    private Long createNewOrderItemNo() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}
