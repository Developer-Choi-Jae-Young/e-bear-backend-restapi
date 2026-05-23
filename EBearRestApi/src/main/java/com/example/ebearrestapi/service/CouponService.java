package com.example.ebearrestapi.service;

import com.example.ebearrestapi.dto.request.CouponDto;
import com.example.ebearrestapi.entity.CouponEntity;
import com.example.ebearrestapi.entity.UserEntity;
import com.example.ebearrestapi.repository.CouponRepository;
import com.example.ebearrestapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    private final UserRepository userRepository;

    /**
     * 발급 가능한 전체 쿠폰리스트 조회
     */
    @Transactional(readOnly = true)
    public List<CouponEntity> list(Long userNo) {
        return couponRepository.list(userNo);
    }

    /**
     * 유저의 보유 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponEntity> userList(Long userNo) {
        return couponRepository.findByUser_UserNo(userNo);
    }

    /**
     * 쿠폰 다운로드/발급 (유저가 쿠폰을 발급)
     */
    @Transactional
    public void issue(CouponDto couponDto, Long userNo) {
        // 발급받을 마스터 쿠폰 조회
        CouponEntity masterCoupon = couponRepository.findById(couponDto.getCouponNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        // 마스터 쿠폰 수량 차감
        // 수량이 0이거나 만료되었으면 use() 내부에서 예외가 발생
        masterCoupon.use();

        // 유저 정보 조회
        UserEntity user = userRepository.getReferenceById(Math.toIntExact(userNo));

        // 유저 개인을 위한 새로운 쿠폰 발급
        CouponEntity userCoupon = CouponEntity.builder()
                .couponName(masterCoupon.getCouponName())
                .couponQuantity(1) // 유저 본인의 사용 가능 횟수
                .salePrice(masterCoupon.getSalePrice())
                .couponType(masterCoupon.getCouponType())
                .useAbledPrice(masterCoupon.getUseAbledPrice())
                .expiredDate(LocalDateTime.now().plusDays(30)) // 일단 30일로 셋팅
                .category(masterCoupon.getCategory())
                .user(user) // 해당 유저 소유로 매핑
                .build();
        couponRepository.save(userCoupon);
    }
}
