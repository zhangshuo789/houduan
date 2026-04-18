package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.CreateBoardRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.UpdateBoardRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.BoardResponse;
import com.volleyball.volleyballcommunitybackend.entity.Board;
import com.volleyball.volleyballcommunitybackend.repository.BoardRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 创建板块
    public BoardResponse createBoard(CreateBoardRequest request) {
        Board board = new Board();
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setIcon(request.getIcon());
        Board saved = boardRepository.save(board);
        return toResponse(saved);
    }

    // 更新板块
    public BoardResponse updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("板块不存在"));
        if (request.getName() != null) {
            board.setName(request.getName());
        }
        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            board.setIcon(request.getIcon());
        }
        Board saved = boardRepository.save(board);
        return toResponse(saved);
    }

    // 删除板块
    public void deleteBoard(Long id) {
        if (!boardRepository.existsById(id)) {
            throw new RuntimeException("板块不存在");
        }
        boardRepository.deleteById(id);
    }

    // 获取所有板块
    public List<BoardResponse> getAllBoardsResponse() {
        return boardRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BoardResponse toResponse(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getIcon(),
                board.getCreatedAt()
        );
    }
}
