package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.PostRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PostDetailResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PostResponse;
import com.volleyball.volleyballcommunitybackend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse post = postService.createPost(request, userId);
        return ResponseEntity.ok(ApiResponse.success("发帖成功", post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostDetailResponse post = postService.getPostById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse post = postService.updatePost(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("更新成功", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
