package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.MessageRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRoomRepository extends JpaRepository<MessageRoomEntity, Long> {

}
