package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.response.ChatJoinResDto;
import com.example.ebearrestapi.dto.response.ChatUserMeResDto;
import com.example.ebearrestapi.entity.ConsultaionEntity;
import com.example.ebearrestapi.entity.MessageRoomEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.ConsultaionRepository;
import com.example.ebearrestapi.repository.MessageRoomRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageRoomService {
    private final MessageRoomRepository messageRoomRepository;
    private final ConsultaionRepository consultaionRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatJoinResDto join(User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("유저정보 없음"));
        ConsultaionEntity consultaion = consultaionRepository.save(ConsultaionEntity.builder().build());
        MessageRoomEntity messageRoom = MessageRoomEntity.builder().consultaion(consultaion).user(userEntity).build();
        MessageRoomEntity newMessageRoom = messageRoomRepository.save(messageRoom);
        return ChatJoinResDto.builder().roomId(newMessageRoom.getMessageRoomNo()).build();
    }

    public ChatUserMeResDto userMe(User user) {
        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("유저정보 없음"));
        return ChatUserMeResDto.builder().userId(userEntity.getUserNo()).build();
    }
}
