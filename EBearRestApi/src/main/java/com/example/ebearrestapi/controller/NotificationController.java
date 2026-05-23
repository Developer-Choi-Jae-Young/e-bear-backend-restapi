package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.BoardDto;
import com.example.ebearrestapi.dto.response.NotificationDetailDto;
import com.example.ebearrestapi.dto.response.NotificationListResponseDto;
import com.example.ebearrestapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @PostMapping("/write")
    public void write(@RequestBody BoardDto boardDto, @AuthenticationPrincipal User user) {
        notificationService.write(boardDto, user);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<Long> notificationNos) {
        notificationService.delete(notificationNos);
    }

    @GetMapping("/list")
    public NotificationListResponseDto list(@AuthenticationPrincipal User user) {
        return notificationService.list(user);
    }

    @GetMapping("/detail/{notificationNo}")
    public NotificationDetailDto detail(@PathVariable Long notificationNo, @AuthenticationPrincipal User user) {
        return notificationService.detail(notificationNo, user);
    }

    @PutMapping("/update/{notificationNo}")
    public void update(@PathVariable Long notificationNo, @RequestBody BoardDto boardDto) {
        notificationService.update(notificationNo, boardDto);
    }
}
