package com.example.ebearrestapi.dto.request;

import lombok.Data;

@Data
public class ChatMessageReqDto {
    private Long roomId;
    private String content;
    private Long senderId;
}
