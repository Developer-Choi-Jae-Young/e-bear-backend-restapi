package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.response.InquiryAdminDto;
import com.example.ebearrestapi.entity.BoardEntity;
import com.example.ebearrestapi.entity.InquiryEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.InquiryRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryAdminService {
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<InquiryAdminDto> list(User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        List<InquiryEntity> inquiryEntityList;

        if (userEntity.isAdmin()) {
            inquiryEntityList = inquiryRepository.findByParentIsNullAndBoard_DelYNOrderByInquiryNoDesc("N");
        } else {
            inquiryEntityList = inquiryRepository.findByParentIsNullAndBoard_DelYNAndProduct_UserOrderByInquiryNoDesc("N", userEntity);
        }

        List<InquiryAdminDto> inquiries = new ArrayList<>();
        for (InquiryEntity inquiryEntity : inquiryEntityList) {
            BoardEntity boardEntity = inquiryEntity.getBoard();

            InquiryEntity replyEntity = inquiryEntity.getChildrenList().isEmpty() ? null : inquiryEntity.getChildrenList().get(0);
            BoardEntity replyBoardEntity = replyEntity == null ? null : replyEntity.getBoard();

            InquiryAdminDto inquiryAdminDto = new InquiryAdminDto(
                    inquiryEntity.getInquiryNo(),
                    inquiryEntity.getProduct().getProductName(),
                    boardEntity.getTitle(),
                    boardEntity.getUser().getUserName(),
                    boardEntity.getRegDate(),
                    replyBoardEntity == null ? null : replyBoardEntity.getRegDate(),
                    replyBoardEntity == null ? null : replyBoardEntity.getUser().getUserName()
            );

            inquiries.add(inquiryAdminDto);
        }

        return inquiries;
    }
}
