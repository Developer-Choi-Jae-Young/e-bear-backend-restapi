package com.example.ebearrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationDetailDto {
    private Long notificationNo;
    private String title;
    private String writer;
    private LocalDateTime regDt;
    private Integer viewCnt;
    private String content;
}
