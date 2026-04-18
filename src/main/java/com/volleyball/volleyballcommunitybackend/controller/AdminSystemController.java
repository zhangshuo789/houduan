package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.CreateBoardRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SensitiveWordRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.UpdateBoardRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.BoardResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.SensitiveWordResponse;
import com.volleyball.volleyballcommunitybackend.service.BoardService;
import com.volleyball.volleyballcommunitybackend.service.SensitiveWordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private final BoardService boardService;
    private final SensitiveWordService sensitiveWordService;

    public AdminSystemController(BoardService boardService, SensitiveWordService sensitiveWordService) {
        this.boardService = boardService;
        this.sensitiveWordService = sensitiveWordService;
    }

    // 板块管理
    @GetMapping("/boards")
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoardList() {
        List<BoardResponse> boards = boardService.getAllBoardsResponse();
        return ResponseEntity.ok(ApiResponse.success(boards));
    }

    @PostMapping("/boards")
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        BoardResponse board = boardService.createBoard(request);
        return ResponseEntity.ok(ApiResponse.success("板块创建成功", board));
    }

    @PutMapping("/boards/{id}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBoardRequest request) {
        BoardResponse board = boardService.updateBoard(id, request);
        return ResponseEntity.ok(ApiResponse.success("板块更新成功", board));
    }

    @DeleteMapping("/boards/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ResponseEntity.ok(ApiResponse.success("板块删除成功", null));
    }

    // 敏感词管理
    @GetMapping("/sensitive-words")
    public ResponseEntity<ApiResponse<List<SensitiveWordResponse>>> getSensitiveWordList() {
        List<SensitiveWordResponse> words = sensitiveWordService.getAll();
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    @PostMapping("/sensitive-words")
    public ResponseEntity<ApiResponse<SensitiveWordResponse>> addSensitiveWord(@Valid @RequestBody SensitiveWordRequest request) {
        SensitiveWordResponse word = sensitiveWordService.add(request);
        return ResponseEntity.ok(ApiResponse.success("敏感词添加成功", word));
    }

    @PutMapping("/sensitive-words/{id}")
    public ResponseEntity<ApiResponse<SensitiveWordResponse>> updateSensitiveWord(
            @PathVariable Long id,
            @Valid @RequestBody SensitiveWordRequest request) {
        SensitiveWordResponse word = sensitiveWordService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("敏感词更新成功", word));
    }

    @DeleteMapping("/sensitive-words/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("敏感词删除成功", null));
    }
}
