package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.CommentRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.CommentResponse;
import com.volleyball.volleyballcommunitybackend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/post/{id}/comment")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable("id") Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse comment = commentService.addComment(postId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("评论成功", comment));
    }

    @GetMapping("/api/post/{id}/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable("id") Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentResponse> comments = commentService.getComments(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @DeleteMapping("/api/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
