package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.CartDto;
import com.example.ebearrestapi.entity.CartEntity;
import com.example.ebearrestapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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
    public List<CartEntity> getCart(@AuthenticationPrincipal User user) {
//        CartDto;
        return cartService.getCart(user);
    }

    @PostMapping(value = "/addCart", name = "장바구니 담기")
    public String addCart(@RequestBody CartDto cartDto, @AuthenticationPrincipal User user) {
        cartService.addCart(cartDto, user);
        return "장바구니 담기 완료";
    }

    @PostMapping(value = "/updateQuantityProd", name = "장바구니 수량 변경")
    public String updateQuantityProd(@RequestBody CartDto cartDto, @AuthenticationPrincipal User user) {
        cartService.updateQuantityProd(cartDto, user);
        return "장바구니 수량 변경 완료";
    }

    @PostMapping(value = "/deleteCart", name = "장바구니 제거")
    public String deleteCart(@RequestBody CartDto cartDto, @AuthenticationPrincipal User user) {
        cartService.deleteCart(cartDto, user);
        return "장바구니 제거 완료";
    }
}
