package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.response.InquiryAdminDto;
import com.example.ebearrestapi.service.InquiryAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("inquiry/admin")
@RequiredArgsConstructor
public class InquiryAdminController {
    private final InquiryAdminService inquiryAdminService;

    @GetMapping("list")
    public List<InquiryAdminDto> list(@AuthenticationPrincipal User user) {
        return inquiryAdminService.list(user);
    }
}
