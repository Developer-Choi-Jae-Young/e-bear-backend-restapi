package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.ReqCartDto;
import com.example.ebearrestapi.dto.request.ResCartDto;
import com.example.ebearrestapi.entity.CartEntity;
import com.example.ebearrestapi.entity.ProductOptionEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.CartRepository;
import com.example.ebearrestapi.repository.ProductOptionRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    private final ProductOptionRepository productOptionRepository;

    private final UserRepository userRepository;

    /**
     * 장바구니 조회
     */
    @Transactional(readOnly = true)
    public List<ResCartDto> getCart(User user) {
        // 유저정보 검증
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("유저정보 없음"));
        List<CartEntity> cartEntityList = cartRepository.findByUser(userEntity);
        List<ResCartDto> resCartDtoList = new ArrayList<>();

        // RESPONSE
        cartEntityList.forEach(cartEntity -> {
            ResCartDto resCartDto = new ResCartDto();
            resCartDto.setCartNo(cartEntity.getCartNo());
            resCartDto.setQuantity(cartEntity.getQuantity());
            resCartDto.setProductOptionNo(cartEntity.getProductOption().getProductOptionNo());
            resCartDto.setProductName(cartEntity.getProductOption().getProduct().getProductName());
            resCartDto.setProductOptionName(cartEntity.getProductOption().getProductOptionName());
            resCartDtoList.add(resCartDto);
        });

        return resCartDtoList;
    }

    /**
     * 장바구니 추가
     */
    @Retryable(
        // 낙관적 락 충돌과 유니크 키 중복 충돌 시 재시도
        retryFor = {ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class},
        maxAttempts = 3, // 최대 3번 시도
        backoff = @Backoff(delay = 50, maxDelay = 150, random = true) // 50 ~ 150ms 사이 랜덤 대기 알아서 처리
    )
    @Transactional(rollbackFor = Exception.class)
    public void addCart(ReqCartDto reqCartDto, User user) {
        // 유저정보 검증
        UserEntity userInfo = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 상품정보 없을 시
        if(reqCartDto.getProductOptionNoList().isEmpty()) {
            throw new IllegalArgumentException("담을 상품정보가 없습니다.");
        }

        // 상품정보 반복문 처리
        for (Long optionNo : reqCartDto.getProductOptionNoList()) {
            // 이미 장바구니에 똑같은 상품 + 옵션이 있는지 확인
            // 옵션정보를 배열로 받음 -> 많은 여러건이 한방에 들어왔을때 어떻게 원자성을 유지하며 처리할것인지.
            Optional<CartEntity> existingCart = cartRepository.findCartItem(userInfo.getUserNo(), optionNo);

            if (existingCart.isPresent()) { // 이미 있다면 수량만 증가
                int addQuantity = reqCartDto.getQuantity() != null && reqCartDto.getQuantity() > 0 ? reqCartDto.getQuantity() : 1;
                existingCart.get().increaseQuantity(addQuantity);
                log.info("장바구니 수량 추가 완료 : {}", optionNo);
                // 수량 증가시 다음 반복단계로 넘기기
                continue;
            } else { // 없다면 새로 생성
                ProductOptionEntity productOption = productOptionRepository.getReferenceById(optionNo);
                CartEntity cartEntity = CartEntity.builder()
                        .user(userInfo)
                        .productOption(productOption)
                        .quantity(reqCartDto.getQuantity() != null ? reqCartDto.getQuantity() : 1) // 기본값 처리
                        .build();
                cartRepository.save(cartEntity);
                log.info("장바구니 신규 담기 완료 : {}", optionNo);
            }
        }
        // 더욱 많은 데이터가 들어올경우 saveAll고려 (벌크)
    }

    /**
     * 장바구니 수량 변경
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantityProd(ReqCartDto reqCartDto, User user) {
        // 트랜잭션 완료처리 시기전까지 여러번 호출 시 어떻게 처리할것인가
        // cartNo 기존 데이터 조회
        CartEntity cartItem = cartRepository.findById(reqCartDto.getCartNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));

        // 해당 장바구니가 현재 요청한 유저의 것이 맞는지 확인
        if (!cartItem.getUser().getUserNo().equals(user.getUsername())) {
            throw new IllegalArgumentException("본인의 장바구니 수량만 변경할 수 있습니다.");
        }

        // 0 이하의 값을 보냈을 때를 대비한 방어 로직
        int newQuantity = reqCartDto.getQuantity();
        if (newQuantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        cartItem.setQuantity(newQuantity);
        log.info("장바구니 수량 업데이트 : {}", cartItem);
    }

    /**
     * 장바구니 삭제
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(ReqCartDto reqCartDto, User user) {
        // 본인의 장바구니 검증
        CartEntity cartItem = cartRepository.findById(reqCartDto.getCartNo()).orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 없습니다."));

        // 지우려는 장바구니가 현재 접속한 유저의 것인지 확인
        if (!cartItem.getUser().getUserNo().equals(user.getUsername())) {
            throw new IllegalArgumentException("본인의 장바구니만 삭제할 수 있습니다.");
        }

        // 삭제여부 "Y" 처리
        cartItem.setDelYN("Y");
        log.info("장바구니 삭제 업데이트 : {}", cartItem);
    }
}
