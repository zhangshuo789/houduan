package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PostResponse;
import com.volleyball.volleyballcommunitybackend.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/api/post/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> favorite(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.favorite(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    @DeleteMapping("/api/post/{id}/unfavorite")
    public ResponseEntity<ApiResponse<Void>> unfavorite(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.unfavorite(postId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
    }

    @GetMapping("/api/post/{id}/favoriteStatus")
    public ResponseEntity<ApiResponse<Boolean>> getFavoriteStatus(
            @PathVariable("id") Long postId,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Boolean status = favoriteService.getFavoriteStatus(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/api/user/{id}/favorites")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserFavorites(
            @PathVariable("id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Page<PostResponse> favorites = favoriteService.getUserFavorites(userId, page, size, request);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
}
