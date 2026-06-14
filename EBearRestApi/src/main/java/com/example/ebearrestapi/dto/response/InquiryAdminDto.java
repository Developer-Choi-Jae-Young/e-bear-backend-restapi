package com.example.ebearrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InquiryAdminDto {
    private Long inquiryNo;
    private String productName;
    private String title;
    private String customer;
    private LocalDateTime regDt;
    private LocalDateTime respondDt;
    private String responder;
}
