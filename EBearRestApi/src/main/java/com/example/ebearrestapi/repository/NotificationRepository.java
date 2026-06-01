package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("""
    select n
    from NotificationEntity n
    join fetch n.board b
    join fetch b.user u
    where b.delYN = 'N'
      and (:startDate is null or b.regDate >= :startDate)
      and (:endDate is null or b.regDate < :endDate)
      and (
            :keyword is null
            or :keyword = ''
            or (
                (:condition = 'title' and lower(b.title) like lower(concat('%', :keyword, '%')))
                or (:condition = 'content' and b.content like concat('%', :keyword, '%'))
                or (:condition = 'writer' and lower(u.userName) like lower(concat('%', :keyword, '%')))
                or (
                    (:condition is null or :condition = '' or :condition = 'all')
                    and (
                        lower(b.title) like lower(concat('%', :keyword, '%'))
                        or b.content like concat('%', :keyword, '%')
                        or lower(u.userName) like lower(concat('%', :keyword, '%'))
                    )
                )
            )
      )
    order by n.notificationNo desc
    """)
    List<NotificationEntity> searchNotifications(
            @Param("condition") String condition,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
