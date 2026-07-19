package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.service.MessageRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {
    private final MessageRoomService messageRoomService;

    @PostMapping("/join")
    public ResponseEntity<?> joinMessageRoom(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(messageRoomService.join(user));
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> userMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.OK).body(messageRoomService.userMe(user));
    }
}
