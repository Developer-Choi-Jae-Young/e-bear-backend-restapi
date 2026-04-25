package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.CartDto;
import com.example.ebearrestapi.entity.CartEntity;
import com.example.ebearrestapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/getCart", name = "장바구니 조회")
    public List<CartEntity> getCart(@RequestBody Long userNo) {
        return cartService.getCart(userNo);
    }

    @PostMapping(value = "/addCart", name = "장바구니 담기")
    public String addCart(@RequestBody CartDto cartDto, Long userNo) {
        cartService.addCart(cartDto, userNo);
        return "장바구니 담기 완료";
    }

    @PostMapping(value = "/updateQuantityProd", name = "장바구니 수량 변경")
    public String updateQuantityProd(@RequestBody CartDto cartDto, Long userNo) {
        cartService.updateQuantityProd(cartDto, userNo);
        return "장바구니 수량 변경 완료";
    }

    @PostMapping(value = "/deleteCart", name = "장바구니 제거")
    public String deleteCart(@RequestBody CartDto cartDto, Long userNo) {
        cartService.deleteCart(cartDto, userNo);
        return "장바구니 제거 완료";
    }
}
