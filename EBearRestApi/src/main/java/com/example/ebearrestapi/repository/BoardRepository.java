package com.example.ebearrestapi.repository;

import com.example.ebearrestapi.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<BoardEntity,Long>  {
}
