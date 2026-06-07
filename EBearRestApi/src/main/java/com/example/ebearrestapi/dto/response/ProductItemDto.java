package com.example.ebearrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemDto {
    private Long id;
    private String imageUrl;
    private String brand;
    private String name;
    private String price;
    private Integer salePercentage;
    private Double rating;
    private Integer reviewCount;
}