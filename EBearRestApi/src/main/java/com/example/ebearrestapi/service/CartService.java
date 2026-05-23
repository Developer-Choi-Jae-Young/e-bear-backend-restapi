package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.CartDto;
import com.example.ebearrestapi.entity.CartEntity;
import com.example.ebearrestapi.entity.ProductOptionEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.CartRepository;
import com.example.ebearrestapi.repository.ProductOptionRepository;
import com.example.ebearrestapi.repository.ProductRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final ProductOptionRepository productOptionRepository;

    private final UserRepository userRepository;

    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public List<CartEntity> getCart(User user) {
//        CartDto
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("유저정보 에러"));
        return cartRepository.findByUser(userEntity);
    }

    /**
     * 장바구니 추가
     */
    @Transactional
    public void addCart(CartDto cartDto, User user) {
        // 트랜잭션
        // 옵션정보를 배열로 받음 -> 많은 여러건이 한방에 들어왔을때 어떻게 원자성을 유지하며 처리할것인지.
        // 이미 장바구니에 똑같은 상품 + 옵션이 있는지 확인
        Optional<CartEntity> existingCart = cartRepository.findCartItem(
            user.getUsername()
            ,cartDto.getProductOptionNo()
        );

        if (existingCart.isPresent()) {
            // 이미 있다면 수량만 증가
            int addQuantity = cartDto.getQuantity() != null && cartDto.getQuantity() > 0 ? cartDto.getQuantity() : 1;
            existingCart.get().increaseQuantity(addQuantity);
        } else {
            // 없다면 새로 생성
            ProductOptionEntity productOption = productOptionRepository.getReferenceById(cartDto.getProductOptionNo());
            UserEntity userInfo = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            CartEntity cartEntity = CartEntity.builder()
                    .user(userInfo)
                    .productOption(productOption)
                    .quantity(cartDto.getQuantity() != null ? cartDto.getQuantity() : 1) // 기본값 처리
                    .build();
            cartRepository.save(cartEntity);
        }
    }

    /**
     * 장바구니 수량 변경
     */
    @Transactional
    public void updateQuantityProd(CartDto cartDto, User user) {
        // 트랜잭션 완료처리 시기전까지 여러번 호출 시 어떻게 처리할것인가
        // cartNo 기존 데이터 조회
        CartEntity cartItem = cartRepository.findById(cartDto.getCartNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));

        // 해당 장바구니가 현재 요청한 유저의 것이 맞는지 확인
        if (!cartItem.getUser().getUserNo().equals(user.getUsername())) {
            throw new IllegalArgumentException("본인의 장바구니 수량만 변경할 수 있습니다.");
        }

        // 0 이하의 값을 보냈을 때를 대비한 방어 로직
        int newQuantity = cartDto.getQuantity();
        if (newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        cartItem.setQuantity(newQuantity);
    }

    /**
     * 장바구니 삭제
     */
    @Transactional
    public void deleteCart(CartDto cartDto, User user) {
        // 본인의 장바구니 검증
        CartEntity cartItem = cartRepository.findById(cartDto.getCartNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 없습니다."));
        // 지우려는 장바구니가 현재 접속한 유저의 것인지 확인
        if (!cartItem.getUser().getUserNo().equals(user.getUsername())) {
            throw new IllegalArgumentException("본인의 장바구니만 삭제할 수 있습니다.");
        }
        cartRepository.delete(cartItem);
    }
}
