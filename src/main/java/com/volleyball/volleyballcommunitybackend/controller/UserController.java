package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.UpdateUserRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.FeedResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserStatsResponse;
import com.volleyball.volleyballcommunitybackend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            HttpServletRequest request) {
        UserResponse user = userService.getUserById(id, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateRequest,
            Authentication authentication,
            HttpServletRequest request) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!currentUserId.equals(id)) {
            throw new RuntimeException("无权限修改此用户信息");
        }
        UserResponse user = userService.updateUser(id, updateRequest, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@PathVariable Long id) {
        UserStatsResponse stats = userService.getUserStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<ApiResponse<Page<FeedResponse>>> getUserFeed(
            @PathVariable Long id,
            Pageable pageable) {
        Page<FeedResponse> feed = userService.getUserFeed(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }
}