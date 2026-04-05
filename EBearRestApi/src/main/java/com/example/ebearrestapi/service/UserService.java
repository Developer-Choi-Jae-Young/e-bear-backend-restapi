package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.SignupDto;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailAuthStore emailAuthStore;

    public UserEntity signup(SignupDto dto) {

        if(!emailAuthStore.isVerified(dto.getEmail())){
            throw new RuntimeException("이메일 인증을 완료해주세요.");
        }
//        emailAuthStore.remove(dto.getEmail());

        userRepository.findByUserId(dto.getId())
                .ifPresent(user -> {
                    throw new RuntimeException("이미 존재하는 아이디입니다.");
                });

        UserEntity user = dto.toEntity(passwordEncoder.encode(dto.getPw()));
        return userRepository.save(user);
    }
}
