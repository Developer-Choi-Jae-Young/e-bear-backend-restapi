package com.example.ebearrestapi.service;

import com.example.ebearrestapi.entity.BoardEntity;
import com.example.ebearrestapi.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    public BoardEntity selectBoardEntity(Long boardNo) {
        return boardRepository.findById(boardNo).orElseThrow(()-> new RuntimeException());
    }
}
