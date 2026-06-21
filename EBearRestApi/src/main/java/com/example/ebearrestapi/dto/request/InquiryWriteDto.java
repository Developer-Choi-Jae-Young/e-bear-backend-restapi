package com.example.ebearrestapi.dto.request;

import lombok.Getter;

@Getter
public class InquiryWriteDto {
    private Long productNo;
    private String title;
    private String content;
}
