package com.example.ebearrestapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResCartDto {
    private Long cartNo;
    private Integer quantity;
    private Long productOptionNo;
    private String productName;
    private String productOptionName;
}
