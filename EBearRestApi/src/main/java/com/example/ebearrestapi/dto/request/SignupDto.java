package com.example.ebearrestapi.dto.request;

import com.example.ebearrestapi.entity.UserEntity;
import lombok.Data;

@Data
public class SignupDto {

    private String id;
    private String pw;
    private String email;
    private String name;
    private String emailAuthCode;

    public UserEntity toEntity(String pw) {
        return UserEntity.builder()
                .userId(id)
                .password(pw)
                .userName(name)
                .email(email)
                .build();
    }
}
