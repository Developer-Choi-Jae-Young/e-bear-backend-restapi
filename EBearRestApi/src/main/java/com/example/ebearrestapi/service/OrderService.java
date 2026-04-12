package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.OrderDto;
import com.example.ebearrestapi.dto.response.OrderSaveResultDto;
import com.example.ebearrestapi.dto.response.OrderSelectListResultDto;
import com.example.ebearrestapi.dto.response.ProductOptionDto;
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
    public OrderSaveResultDto saveOrder(OrderDto orderDto, User user) {
        UserEntity newUser = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        OrderPaymentEntity newOrderPayment = orderPaymentRepository.save(OrderPaymentEntity.builder()
                .orderStatus(OrderStatus.PAYMENT_WAIT)
                .deliveryAddr(orderDto.getAddress())
                .tel(orderDto.getTel())
                .email(orderDto.getEmail())
                .user(newUser)
                .deliveryRequired(orderDto.getDeliveryRequired()).build());

        orderDto.getProductOptionList().forEach(productOption -> {
            int quantity = productOption.getQuantity();
            ProductOptionEntity productOptionEntity = productOptionRepository.findById(productOption.getProductOptionId()).orElseThrow(() -> new RuntimeException("Product Option Not Found"));
            productOptionEntity.decreaseProductOptionQuantity(quantity);

            MyCouponEntity myCoupon = myCouponRepository.findById(productOption.getCouponId()).orElseThrow(() -> new RuntimeException("Coupon Not Found"));
            OrderItemEntity newOrderItem = orderItemRepository.save(OrderItemEntity.builder()
                    .orderPayment(newOrderPayment)
                    .productOption(productOptionEntity)
                    .myCoupon(myCoupon)
                    .quantity(quantity).build());
        });

        return OrderSaveResultDto.builder().orderPaymentId(newOrderPayment.getOrderPaymentId()).build();
    }

    public OrderPaymentEntity selectOrder(Long orderPaymentId) {
        return orderPaymentRepository.findById(orderPaymentId).orElseThrow(() -> new RuntimeException("OrderPaymentId not found"));
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
}
