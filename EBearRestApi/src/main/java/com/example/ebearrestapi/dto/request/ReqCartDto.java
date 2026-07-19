package com.example.ebearrestapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqCartDto {
    private Long cartNo;
    private Integer quantity = 1;
    private List<Long> productOptionNoList;
}
