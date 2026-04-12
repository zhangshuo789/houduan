package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/api/post/{id}/like")
    public ResponseEntity<ApiResponse<Void>> like(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        likeService.like(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    @DeleteMapping("/api/post/{id}/unlike")
    public ResponseEntity<ApiResponse<Void>> unlike(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        likeService.unlike(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消点赞成功", null));
    }

    @GetMapping("/api/post/{id}/likeStatus")
    public ResponseEntity<ApiResponse<Boolean>> getLikeStatus(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Boolean status = likeService.getLikeStatus(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
