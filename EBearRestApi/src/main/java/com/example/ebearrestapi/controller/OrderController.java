package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.OrderDto;
import com.example.ebearrestapi.dto.response.OrderResultDto;
import com.example.ebearrestapi.service.OrderService;
import com.example.ebearrestapi.vo.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/save")
    public ResponseEntity<?> saveOrder(@RequestBody OrderDto orderDto, @AuthenticationPrincipal UserDetail userDetail) {
        OrderResultDto orderResultDto = null;
        orderService.save(orderDto);
        return ResponseEntity.status(HttpStatus.OK).body(orderResultDto);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateOrder(@RequestBody OrderDto orderDto) {
        OrderResultDto orderResultDto = orderService.update(orderDto);
        return ResponseEntity.status(HttpStatus.OK).body(orderResultDto);
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteOrder(@RequestBody OrderDto orderDto) {
        OrderResultDto orderResultDto = orderService.delete(orderDto);
        return ResponseEntity.status(HttpStatus.OK).body(orderResultDto);
    }
}
