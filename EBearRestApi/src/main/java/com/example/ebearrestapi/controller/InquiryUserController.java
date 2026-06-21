package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.InquiryWriteDto;
import com.example.ebearrestapi.service.InquiryUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("inquiry/user")
@RequiredArgsConstructor
public class InquiryUserController {
    private final InquiryUserService inquiryUserService;

    @PostMapping("/write")
    public void write(@RequestBody InquiryWriteDto inquiryWriteDto, @AuthenticationPrincipal User user) {
        inquiryUserService.write(inquiryWriteDto, user);
    }
}
