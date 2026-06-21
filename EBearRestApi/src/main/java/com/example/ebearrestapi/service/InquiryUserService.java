package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.InquiryWriteDto;
import com.example.ebearrestapi.entity.BoardEntity;
import com.example.ebearrestapi.entity.InquiryEntity;
import com.example.ebearrestapi.entity.ProductEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.BoardRepository;
import com.example.ebearrestapi.repository.InquiryRepository;
import com.example.ebearrestapi.repository.ProductRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryUserService {
    private final InquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void write(InquiryWriteDto inquiryWriteDto, User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        ProductEntity productEntity = productRepository.findById(inquiryWriteDto.getProductNo()).orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        BoardEntity boardEntity = BoardEntity.builder().title(inquiryWriteDto.getTitle()).content(inquiryWriteDto.getContent()).user(userEntity).build();
        boardRepository.save(boardEntity);

        InquiryEntity inquiryEntity = InquiryEntity.builder().board(boardEntity).product(productEntity).build();
        inquiryRepository.save(inquiryEntity);
    }
}
