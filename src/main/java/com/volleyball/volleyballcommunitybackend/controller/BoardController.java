package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PostResponse;
import com.volleyball.volleyballcommunitybackend.entity.Board;
import com.volleyball.volleyballcommunitybackend.service.BoardService;
import com.volleyball.volleyballcommunitybackend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;
    private final PostService postService;

    public BoardController(BoardService boardService, PostService postService) {
        this.boardService = boardService;
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Board>>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return ResponseEntity.ok(ApiResponse.success(boards));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getBoardPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        boardService.getBoardById(id);
        Page<PostResponse> posts = postService.getPostsByBoardId(id, page, size, request);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
}
