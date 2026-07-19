package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.ChatMessageReqDto;
import com.example.ebearrestapi.entity.MessageEntity;
import com.example.ebearrestapi.service.MessageRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/rooms/admin")
    public ResponseEntity<?> chatRoomList() {
        return ResponseEntity.status(HttpStatus.OK).body(messageRoomService.getChatRoomList());
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal User user, @RequestBody ChatMessageReqDto chatMessageReqDto) {
        return ResponseEntity.status(HttpStatus.OK).body(messageRoomService.saveChatMessage(chatMessageReqDto));
    }

    @GetMapping("/rooms/{id}/messages")
    public ResponseEntity<?> findMessage(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(messageRoomService.findMessage(id));
    }
}
