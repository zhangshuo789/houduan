package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.Board;
import com.volleyball.volleyballcommunitybackend.repository.BoardRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @PostConstruct
    public void initBoards() {
        if (boardRepository.count() == 0) {
            boardRepository.save(new Board(null, "技术讨论", "技战术分析、训练方法交流", "🏐", null));
            boardRepository.save(new Board(null, "赛事资讯", "国内外排球赛事报道", "🏆", null));
            boardRepository.save(new Board(null, "装备评测", "球鞋、球服、护具等装备测评", "👟", null));
            boardRepository.save(new Board(null, "约球专区", "组队约球、招募球员", "🤝", null));
        }
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Board getBoardById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("板块不存在"));
    }
}
