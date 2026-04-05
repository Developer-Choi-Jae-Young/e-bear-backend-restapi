package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.SignupDto;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.service.UserDetailService;
import com.example.ebearrestapi.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

public class SignupController {

    private final UserService userService;

    @PostMapping("/signup")
    public UserEntity signup(@RequestBody SignupDto req) {
        return userService.signup(req);
    }
}


