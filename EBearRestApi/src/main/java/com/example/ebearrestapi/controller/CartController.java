package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.ReqCartDto;
import com.example.ebearrestapi.dto.request.ResCartDto;
import com.example.ebearrestapi.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public List<ResCartDto> getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(user);
    }

    @PostMapping(value = "/addCart", name = "장바구니 담기")
    public ResponseEntity<?> addCart(@RequestBody ReqCartDto reqCartDto, @AuthenticationPrincipal User user) {
        cartService.addCart(reqCartDto, user);
        return ResponseEntity.ok("장바구니 담기 성공");
    }

    @PostMapping(value = "/updateQuantityProd", name = "장바구니 수량 변경")
    public String updateQuantityProd(@RequestBody ReqCartDto reqCartDto, @AuthenticationPrincipal User user) {
        cartService.updateQuantityProd(reqCartDto, user);
        return "장바구니 수량 변경 완료";
    }

    @PostMapping(value = "/deleteCart", name = "장바구니 제거")
    public String deleteCart(@RequestBody ReqCartDto reqCartDto, @AuthenticationPrincipal User user) {
        cartService.deleteCart(reqCartDto, user);
        return "장바구니 제거 완료";
    }
}
