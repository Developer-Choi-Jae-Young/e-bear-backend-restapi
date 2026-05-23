package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.dto.request.AlarmDto;
import com.example.ebearrestapi.entity.AlarmEntity;
import com.example.ebearrestapi.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping(value = "/list", name = "알림리스트 조회")
    public List<AlarmEntity> list(@RequestParam AlarmDto alarmDto) {
        return alarmService.list(alarmDto);
    }

}