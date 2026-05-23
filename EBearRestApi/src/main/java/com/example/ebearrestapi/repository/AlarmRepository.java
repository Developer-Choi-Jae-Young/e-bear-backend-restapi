package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.AlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<AlarmEntity, Long> {
    // 유저의 모든 알람 목록 조회
    List<AlarmEntity> findByUser_UserNoOrderByAlarmNoDesc(Long userNo);
}