package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.BoardDto;
import com.example.ebearrestapi.dto.response.NotificationDetailDto;
import com.example.ebearrestapi.dto.response.NotificationDto;
import com.example.ebearrestapi.service.NotificationService;
import com.example.ebearrestapi.vo.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @PostMapping("/write")
    public void write(@RequestBody BoardDto boardDto, @AuthenticationPrincipal UserDetail userDetail) {
        notificationService.write(boardDto, userDetail);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<Long> notificationNos) {
        notificationService.delete(notificationNos);
    }

    @GetMapping("/list")
    public List<NotificationDto> list() {
        return notificationService.list();
    }

    @GetMapping("/detail/{notificationNo}")
    public NotificationDetailDto detail(@PathVariable Long notificationNo) {
        return notificationService.detail(notificationNo);
    }
}
