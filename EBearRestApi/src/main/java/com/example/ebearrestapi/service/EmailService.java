package com.example.ebearrestapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailAuthStore emailAuthStore;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 인증코드 발송
    public void sendAuthCode(String email) {

        String code = String.valueOf((int)((Math.random()*900000)+100000));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[eBear] 이메일 인증코드");
        message.setText("인증코드 : " + code);

        mailSender.send(message);
        emailAuthStore.saveCode(email, code);
    }

    // 인증코드 검증
    public void verify(String email, String code){
        if(!emailAuthStore.verifyCode(email, code)){
            throw new RuntimeException("인증코드가 틀렸습니다.");
        }
    }
}