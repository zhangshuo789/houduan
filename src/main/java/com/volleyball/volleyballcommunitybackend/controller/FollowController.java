package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.FollowStatusResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/api/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        followService.followUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("关注成功", null));
    }

    @DeleteMapping("/api/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        followService.unfollowUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消关注成功", null));
    }

    @GetMapping("/api/follow/{userId}/status")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> getFollowStatus(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        FollowStatusResponse status = followService.getFollowStatus(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/api/user/{userId}/following")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFollowingList(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable,
            HttpServletRequest request) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<UserResponse> list = followService.getFollowingList(userId, currentUserId, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/api/user/{userId}/followers")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFollowerList(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable,
            HttpServletRequest request) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<UserResponse> list = followService.getFollowerList(userId, currentUserId, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/api/user/{userId}/friends")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFriendsList(
            @PathVariable Long userId,
            Pageable pageable,
            HttpServletRequest request) {
        Page<UserResponse> list = followService.getFriendsList(userId, pageable, request);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
