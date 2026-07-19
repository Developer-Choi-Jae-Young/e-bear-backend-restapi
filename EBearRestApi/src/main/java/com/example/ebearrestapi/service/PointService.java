package com.example.ebearrestapi.service;

import com.example.ebearrestapi.entity.PointEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.PointRepository;
import com.example.ebearrestapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    @Transactional
    public void deductPoint(Long userNo, int amount) {
        UserEntity user = userRepository.findByUserNoWithLock(userNo)
                .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

        int currentPoint = pointRepository.sumUseAmountByUserNo(userNo);
        if (currentPoint < amount) {
            throw new RuntimeException("포인트 잔액이 부족합니다.");
        }

        pointRepository.save(PointEntity.builder().user(user).useAmount(-amount).build());
    }
}
