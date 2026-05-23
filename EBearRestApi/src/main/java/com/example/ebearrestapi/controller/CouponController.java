package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.CouponDto;
import com.example.ebearrestapi.entity.CouponEntity;
import com.example.ebearrestapi.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping(value = "/list", name = "발급 가능한 전체 쿠폰리스트 조회")
    public List<CouponEntity> list(@RequestParam Long userId) {
        return couponService.list(userId);
    }

    @PostMapping(value = "/userList", name = "유저의 보유 쿠폰 목록 조회")
    public List<CouponEntity> userList(@RequestParam Long userId) {
        return couponService.userList(userId);
    }

    @PostMapping(value = "/issue", name = "유저에게 쿠폰 발급")
    public String issue(@RequestParam CouponDto couponDto, Long userId) {
        couponService.issue(couponDto, userId);
        return "발급 완료";
    }
}