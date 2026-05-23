package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.AlarmDto;
import com.example.ebearrestapi.entity.AlarmEntity;
import com.example.ebearrestapi.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    /**
     * 알람 리스트 전체 조회
     */
    @Transactional(readOnly = true)
    public List<AlarmEntity> list(AlarmDto alarmDto) {
        Long userNo = alarmDto.getUserNo();
        return alarmRepository.findByUser_UserNoOrderByAlarmNoDesc(userNo);
    }

}
