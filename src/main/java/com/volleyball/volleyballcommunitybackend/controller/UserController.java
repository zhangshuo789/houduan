package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.UpdateUserRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.service.UserService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        if (!currentUserId.equals(id)) {
            throw new RuntimeException("无权限修改此用户信息");
        }
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", user));
    }
}
