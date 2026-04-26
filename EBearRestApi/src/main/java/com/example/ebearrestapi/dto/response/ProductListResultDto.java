package com.example.ebearrestapi.dto.response;

import com.example.ebearrestapi.etc.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResultDto {
    private Long productId;
    private String productName;
    private String seller;
    private LocalDate regDttm;
    private String productStatus;
}
