package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.BoardDto;
import com.example.ebearrestapi.dto.response.NotificationDetailDto;
import com.example.ebearrestapi.dto.response.NotificationDto;
import com.example.ebearrestapi.entity.BoardEntity;
import com.example.ebearrestapi.entity.NotificationEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.NotificationRepository;
import com.example.ebearrestapi.vo.UserDetail;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final BoardService boardService;

    @Transactional
    public void write(BoardDto boardDto, UserDetail userDetail) {
        // 공지사항 테이블에 데이터를 삽입한다.
        UserEntity user = userDetail.getUser();
        BoardEntity board = BoardEntity.builder().
                title(boardDto.getTitle()).content(boardDto.getContent()).user(user).build();
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
    public List<NotificationDto> list() {
        List<NotificationEntity> notificationEntityList = notificationRepository.findByBoard_DelYNOrderByNotificationNoDesc("N");
        List<NotificationDto> result = new ArrayList<>();

        for (NotificationEntity notificationEntity : notificationEntityList) {
            BoardEntity boardEntity = notificationEntity.getBoard();
            NotificationDto notificationDto = new NotificationDto(
                    notificationEntity.getNotificationNo(),
                    boardEntity.getTitle(),
                    boardEntity.getUser().getUserName(),
                    boardEntity.getRegDate(),
                    boardEntity.getViewCnt()
            );
            result.add(notificationDto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public NotificationDetailDto detail(Long notificationNo) {
        NotificationEntity notification = notificationRepository.findDetailByNotificationNo(notificationNo).orElseThrow(() -> new RuntimeException("공지사항이 존재하지 않습니다."));
        BoardEntity board = notification.getBoard();

        return new NotificationDetailDto(
                notification.getNotificationNo(),
                board.getTitle(),
                board.getUser().getUserName(),
                board.getRegDate(),
                board.getViewCnt(),
                board.getContent()
        );
    }
}
