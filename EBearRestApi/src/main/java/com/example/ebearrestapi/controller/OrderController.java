package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.OrderDto;
import com.example.ebearrestapi.dto.response.OrderSaveResultDto;
import com.example.ebearrestapi.dto.response.OrderSelectListResultDto;
import com.example.ebearrestapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/save")
    public ResponseEntity<?> saveOrder(@RequestBody OrderDto orderDto, @AuthenticationPrincipal User user) {
        OrderSaveResultDto orderSaveResultDto = orderService.saveOrder(orderDto, user);
        return ResponseEntity.status(HttpStatus.OK).body(orderSaveResultDto);
    }

    @GetMapping("/list")
    public ResponseEntity<?> listOrder(Pageable pageable) {
        List<OrderSelectListResultDto> orderSaveResultDto = orderService.selectList(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(orderSaveResultDto);
    }
}
