package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.SignupDto;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.UserRepository;
import com.example.ebearrestapi.vo.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity user = (UserEntity) userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User Not Found"));
        return new UserDetail(user);
    }
}
