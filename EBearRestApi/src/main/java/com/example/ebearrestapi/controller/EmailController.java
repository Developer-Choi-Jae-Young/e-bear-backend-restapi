package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public String send(@RequestBody Map<String,String> body){
        emailService.sendAuthCode(body.get("email"));
        return "메일 전송 완료";
    }

    @PostMapping("/verify")
    public String verify(@RequestBody Map<String,String> body){
        emailService.verify(body.get("email"), body.get("code"));
        return "인증 완료";
    }
}
