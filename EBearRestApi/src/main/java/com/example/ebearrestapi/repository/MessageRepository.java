package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.ConsultaionEntity;
import com.example.ebearrestapi.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findByConsultation(ConsultaionEntity consultation);
}
