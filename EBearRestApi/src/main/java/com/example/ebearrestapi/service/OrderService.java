package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.OrderDto;
import com.example.ebearrestapi.dto.response.OrderResultDto;
import com.example.ebearrestapi.entity.*;
import com.example.ebearrestapi.etc.OrderStatus;
import com.example.ebearrestapi.repository.OrderListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderListRepository orderListRepository;

    @Transactional
    public OrderResultDto save(OrderDto orderDto) {
        if (orderDto.isCart()) {
            CartEntity cart = CartEntity.builder().build();
            saveCartOrder(cart, orderDto);
        } else {
            UserEntity user = UserEntity.builder().build();
            ProductOptionEntity option = ProductOptionEntity.builder().build();
            saveDirectOrder(user, option, orderDto);
        }

        return OrderResultDto.builder().build();
    }

    public OrderResultDto update(OrderDto orderDto) {
        OrderEntity order = OrderEntity.builder().build();


        return OrderResultDto.builder().build();
    }

    public OrderResultDto delete(OrderDto orderDto) {
        OrderEntity order = OrderEntity.builder().build();
        return OrderResultDto.builder().build();
    }

    @Transactional
    public void updateOrderInfo(OrderDto dto) {
        OrderPaymentEntity orderPaymentEntity = orderListRepository.findById(dto.getOrderId()).orElseThrow(() -> new RuntimeException("Order Not Found"));
        orderPaymentEntity.updateDeliveryInfo(dto.getAddress(), dto.getTel(), dto.getEmail(), dto.getDeliveryRequired());
    }

    @Transactional
    public OrderPaymentEntity saveDirectOrder(UserEntity user, ProductOptionEntity option, OrderDto dto) {
        OrderEntity order = OrderEntity.builder()
                .orderStatus(OrderStatus.PENDING)
                .deliveryAddr(dto.getAddress())
                .tel(dto.getTel())
                .email(dto.getEmail())
                .user(user)
                .productOption(option)
                .quantity(dto.getQuantity())
                .build();

        return orderListRepository.save(order);
    }

    @Transactional
    public OrderPaymentEntity saveCartOrder(CartEntity cart, OrderDto dto) {
        OrderCartEntity orderCart = OrderCartEntity.builder()
                .orderStatus(OrderStatus.PENDING)
                .deliveryAddr(dto.getAddress())
                .tel(dto.getTel())
                .cart(cart)
                .build();

        return orderListRepository.save(orderCart);
    }
}
