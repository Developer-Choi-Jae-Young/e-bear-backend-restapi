package com.example.ebearrestapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationListResponseDto {
    private List<NotificationDto> notifications;
    private boolean isAdmin;
}
