package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.ChatMessageReqDto;
import com.example.ebearrestapi.dto.response.*;
import com.example.ebearrestapi.entity.ConsultaionEntity;
import com.example.ebearrestapi.entity.MessageEntity;
import com.example.ebearrestapi.entity.MessageRoomEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.etc.Validate;
import com.example.ebearrestapi.repository.ConsultaionRepository;
import com.example.ebearrestapi.repository.MessageRepository;
import com.example.ebearrestapi.repository.MessageRoomRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageRoomService {
    private final MessageRoomRepository messageRoomRepository;
    private final ConsultaionRepository consultaionRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

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

    public List<MessageRoomListDto> getChatRoomList() {
        List<MessageRoomEntity> messageRoomEntityList = messageRoomRepository.findAll();

        return messageRoomEntityList.stream().map(item -> {
                    List<MessageEntity> messageEntityList = messageRepository.findByConsultation(item.getConsultaion());
                    long notReadMessageCnt = messageEntityList.stream().filter(data -> data.getValidate().equals(Validate.UNCHECK)).count();
                    String message = messageEntityList.stream().max(Comparator.comparing(MessageEntity::getRegDate)).map(MessageEntity::getMessage).orElse("");
                    LocalDateTime messageDate = messageEntityList.stream().max(Comparator.comparing(MessageEntity::getRegDate)).map(MessageEntity::getRegDate).orElse(null);
                    return MessageRoomListDto.builder().id(item.getMessageRoomNo()).title(item.getMessageRoomNo() + "번방 - " + item.getUser().getUserName() + "님").message(message).notReadMessageCnt((int) notReadMessageCnt).messageDate(messageDate).build();
                }).toList();
    }

    @Transactional
    public ChatMessageResDto saveChatMessage(ChatMessageReqDto chatMessageReqDto) {
        MessageRoomEntity messageRoom = messageRoomRepository.findById(chatMessageReqDto.getRoomId()).orElseThrow(() -> new RuntimeException("Not Found ChatRoom"));
        ConsultaionEntity consultaionEntity = messageRoom.getConsultaion();
        UserEntity user = userRepository.findById(chatMessageReqDto.getSenderId()).orElseThrow(() -> new RuntimeException("Not Found User"));
        messageRepository.save(MessageEntity.builder().message(chatMessageReqDto.getContent()).messageRoom(messageRoom).consultation(consultaionEntity).user(user).build());
        return ChatMessageResDto.builder().success(true).build();
    }

    public List<ChatMessage> findMessage(Long id) {
        MessageRoomEntity messageRoomEntity = messageRoomRepository.findById(id).orElseThrow(() -> new RuntimeException("Not Found Message"));
        List<MessageEntity> messageEntityList = messageRepository.findByConsultation(messageRoomEntity.getConsultaion());
        return messageEntityList.stream().map(item -> ChatMessage.builder().roomId(messageRoomEntity.getMessageRoomNo()).content(item.getMessage()).senderId(item.getUser().getUserNo()).regDate(item.getRegDate()).build()).toList();
    }
}
