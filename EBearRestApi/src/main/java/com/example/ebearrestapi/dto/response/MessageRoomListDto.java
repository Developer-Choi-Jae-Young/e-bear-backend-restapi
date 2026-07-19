package com.example.ebearrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRoomListDto {
    private Long id;
    private String title;
    private String message;
    private int notReadMessageCnt;
    private LocalDateTime messageDate;
}
