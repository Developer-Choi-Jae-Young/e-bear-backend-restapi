package com.example.ebearrestapi.service;

import com.example.ebearrestapi.entity.StateCodeEntity;
import com.example.ebearrestapi.etc.StateCode;
import com.example.ebearrestapi.repository.StateCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StateCodeService {
    private final StateCodeRepository stateCodeRepository;

    @Transactional
    public void save(Long stateCode, String stateCodeName) {
        if (!stateCodeRepository.existsById(stateCode)) {
            stateCodeRepository.save(StateCodeEntity.builder().stateCodeNo(stateCode).stateName(stateCodeName).build());
        }
    }

    public StateCodeEntity findByStateCodeNo(StateCode stateCodeNo) {
        return stateCodeRepository.findById(stateCodeNo.getValue()).orElseThrow(() -> new RuntimeException("상태코드가 없습니다."));
    }
}
