package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.SetRoleRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SetUserStatusRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserManagementResponse;
import com.volleyball.volleyballcommunitybackend.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean disabled) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementResponse> users = adminUserService.getUserList(keyword, disabled, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> setUserRole(
            @PathVariable Long id,
            @Valid @RequestBody SetRoleRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        adminUserService.setUserRole(id, request.getRoleId());
        return ResponseEntity.ok(ApiResponse.success("角色设置成功", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody SetUserStatusRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        adminUserService.setUserStatus(id, request.getDisabled(), request.getReason(), adminId);
        return ResponseEntity.ok(ApiResponse.success("状态设置成功", null));
    }
}
