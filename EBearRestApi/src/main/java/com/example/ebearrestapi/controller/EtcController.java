package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.response.ProductStateResultDto;
import com.example.ebearrestapi.service.EtcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/etc")
@RequiredArgsConstructor
public class EtcController {
    private final EtcService etcService;

    @GetMapping("/proudct/state/list")
    public ResponseEntity<?> productStateList() {
        List<ProductStateResultDto> productStateResult = etcService.productStateList();
        return ResponseEntity.status(HttpStatus.OK).body(productStateResult);
    }
}
