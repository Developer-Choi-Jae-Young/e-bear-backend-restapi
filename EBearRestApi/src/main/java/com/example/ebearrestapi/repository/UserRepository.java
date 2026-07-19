package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUserId(String userId);

    // 결제 로직에서 사용됨, 해당 Row에 DB 락 검
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.userNo = :userNo")
    Optional<UserEntity> findByUserNoWithLock(@Param("userNo") Long userNo);
}
