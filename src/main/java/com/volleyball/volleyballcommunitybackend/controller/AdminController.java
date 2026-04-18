package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.HandleReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SetRoleRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SetUserStatusRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PendingCountResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ReportResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserManagementResponse;
import com.volleyball.volleyballcommunitybackend.service.AdminUserService;
import com.volleyball.volleyballcommunitybackend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;
    private final ReportService reportService;

    public AdminController(AdminUserService adminUserService, ReportService reportService) {
        this.adminUserService = adminUserService;
        this.reportService = reportService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserManagementResponse>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean disabled) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementResponse> users = adminUserService.getUserList(keyword, disabled, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> setUserRole(
            @PathVariable Long id,
            @Valid @RequestBody SetRoleRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        adminUserService.setUserRole(id, request.getRoleId());
        return ResponseEntity.ok(ApiResponse.success("角色设置成功", null));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody SetUserStatusRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        adminUserService.setUserStatus(id, request.getDisabled(), request.getReason(), adminId);
        return ResponseEntity.ok(ApiResponse.success("状态设置成功", null));
    }

    // ==================== 举报管理接口 ====================

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReportList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReportResponse> reports = reportService.getReportList(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<Void>> submitReport(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        reportService.submitReport(request, userId);
        return ResponseEntity.ok(ApiResponse.success("举报提交成功", null));
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<Void>> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody HandleReportRequest request,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        reportService.handleReport(id, request, adminId);
        return ResponseEntity.ok(ApiResponse.success("举报处理成功", null));
    }

    @GetMapping("/reports/pending")
    public ResponseEntity<ApiResponse<PendingCountResponse>> getPendingCount() {
        long count = reportService.getPendingCount();
        return ResponseEntity.ok(ApiResponse.success(new PendingCountResponse(count)));
    }
}
