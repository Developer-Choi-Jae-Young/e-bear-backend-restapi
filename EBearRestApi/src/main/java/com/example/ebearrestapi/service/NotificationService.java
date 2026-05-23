package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.BoardDto;
import com.example.ebearrestapi.dto.response.NotificationDetailDto;
import com.example.ebearrestapi.dto.response.NotificationDto;
import com.example.ebearrestapi.dto.response.NotificationListResponseDto;
import com.example.ebearrestapi.entity.BoardEntity;
import com.example.ebearrestapi.entity.NotificationEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.NotificationRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void write(BoardDto boardDto, User user) {
        // 공지사항 테이블에 데이터를 삽입한다.
        UserEntity newUser = userRepository.findByUserId(user.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BoardEntity board = BoardEntity.builder().
                title(boardDto.getTitle()).content(boardDto.getContent()).user(newUser).build();
        notificationRepository.save(NotificationEntity.builder().board(board).build());
    }

    @Transactional
    public void delete(List<Long> notificationNos) {
        List<NotificationEntity> notifications = notificationRepository.findAllById(notificationNos);

        for (NotificationEntity notification : notifications) {
            notification.getBoard().setDelYN("Y");
        }
    }

    @Transactional(readOnly = true)
    public NotificationListResponseDto list(User user) {
        List<NotificationEntity> notificationEntityList = notificationRepository.findByBoard_DelYNOrderByNotificationNoDesc("N");
        List<NotificationDto> notifications = new ArrayList<>();

        for (NotificationEntity notificationEntity : notificationEntityList) {
            BoardEntity boardEntity = notificationEntity.getBoard();
            NotificationDto notificationDto = new NotificationDto(
                    notificationEntity.getNotificationNo(),
                    boardEntity.getTitle(),
                    boardEntity.getUser().getUserName(),
                    boardEntity.getRegDate(),
                    boardEntity.getViewCnt()
            );
            notifications.add(notificationDto);
        }
        return new NotificationListResponseDto(notifications, isAdmin(user));
    }


    @Transactional
    public NotificationDetailDto detail(Long notificationNo, User user) {
        NotificationEntity notification = notificationRepository.findDetailByNotificationNo(notificationNo).orElseThrow(() -> new RuntimeException("공지사항이 존재하지 않습니다."));
        BoardEntity board = notification.getBoard();

        board.setViewCnt(board.getViewCnt() + 1);

        return new NotificationDetailDto(
                notification.getNotificationNo(),
                board.getTitle(),
                board.getUser().getUserName(),
                board.getRegDate(),
                board.getViewCnt(),
                board.getContent(),
                isAdmin(user)
        );
    }

    @Transactional
    public void update(Long notificationNo, BoardDto boardDto) {
        NotificationEntity notification = notificationRepository.findById(notificationNo).orElseThrow(() -> new RuntimeException("공지사항이 존재하지 않습니다."));
        BoardEntity board = notification.getBoard();

        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
    }

    private boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }

        UserEntity userEntity = userRepository.findByUserId(user.getUsername()).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return userEntity.isAdmin();
    }
}
