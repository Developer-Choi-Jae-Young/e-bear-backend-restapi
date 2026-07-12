package com.example.ebearrestapi.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryUserListDto {
    private Long inquiryNo;
    private Long productNo;
    private String brandName;
    private String productName;
    private String productImageUrl;
    private String title;
    private String content;
    private LocalDateTime regDate;
    private boolean answered;
    private String answerContent;
    private LocalDateTime answerRegDate;
}
