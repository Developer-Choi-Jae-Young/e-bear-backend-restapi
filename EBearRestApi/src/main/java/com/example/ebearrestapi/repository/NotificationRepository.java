package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Long> {
    List<NotificationEntity> findByBoard_DelYNOrderByNotificationNoDesc(String boardDelYN);

    @Query("""
        select n
        from NotificationEntity n
        join fetch n.board b
        join fetch b.user u
        where n.notificationNo = :notificationNo and b.delYN = 'N'
    """)
    Optional<NotificationEntity> findDetailByNotificationNo(Long notificationNo);
}
