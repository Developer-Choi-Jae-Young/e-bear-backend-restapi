package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.response.ProductStateResultDto;
import com.example.ebearrestapi.etc.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtcService {

    public List<ProductStateResultDto> productStateList() {
        ProductStatus[] productStatuses = ProductStatus.values();
        return Arrays.stream(productStatuses).map(ProductStateResultDto::of).toList();
    }
}
