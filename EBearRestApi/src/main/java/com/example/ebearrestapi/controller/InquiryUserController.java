package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.InquiryWriteDto;
import com.example.ebearrestapi.dto.response.InquiryUserListResponseDto;
import com.example.ebearrestapi.service.InquiryUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("inquiry/user")
@RequiredArgsConstructor
public class InquiryUserController {
    private final InquiryUserService inquiryUserService;

    @PostMapping("/write")
    public void write(@RequestBody InquiryWriteDto inquiryWriteDto, @AuthenticationPrincipal User user) {
        inquiryUserService.write(inquiryWriteDto, user);
    }

    @GetMapping("/list")
    public InquiryUserListResponseDto list(@AuthenticationPrincipal User user) {
        return inquiryUserService.getMyInquiryList(user);
    }
}
