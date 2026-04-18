package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.ChangeGroupOwnerRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.HandleReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReportRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SetRoleRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.SetUserStatusRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.UpdateEventStatusRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ContentStatsResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.PendingCountResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ReportResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.StatsOverviewResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserManagementResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserStatsResponse;
import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.service.AdminEventService;
import com.volleyball.volleyballcommunitybackend.service.AdminGroupService;
import com.volleyball.volleyballcommunitybackend.service.AdminUserService;
import com.volleyball.volleyballcommunitybackend.service.ReportService;
import com.volleyball.volleyballcommunitybackend.service.StatsService;
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
    private final StatsService statsService;
    private final AdminEventService adminEventService;
    private final AdminGroupService adminGroupService;

    public AdminController(AdminUserService adminUserService, ReportService reportService,
                           StatsService statsService, AdminEventService adminEventService,
                           AdminGroupService adminGroupService) {
        this.adminUserService = adminUserService;
        this.reportService = reportService;
        this.statsService = statsService;
        this.adminEventService = adminEventService;
        this.adminGroupService = adminGroupService;
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

    // ==================== 运营统计接口 ====================

    @GetMapping("/stats/overview")
    public ResponseEntity<ApiResponse<StatsOverviewResponse>> getStatsOverview() {
        StatsOverviewResponse stats = statsService.getOverview();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/stats/users")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        UserStatsResponse stats = statsService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/stats/content")
    public ResponseEntity<ApiResponse<ContentStatsResponse>> getContentStats() {
        ContentStatsResponse stats = statsService.getContentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==================== 赛事管理接口 ====================

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Page<Event>>> getEventList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = adminEventService.getEventList(pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PutMapping("/events/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateEventStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventStatusRequest request) {
        adminEventService.updateEventStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("赛事状态更新成功", null));
    }

    @GetMapping("/events/{id}/registrations")
    public ResponseEntity<ApiResponse<Page<EventRegistration>>> getEventRegistrations(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EventRegistration> registrations = adminEventService.getEventRegistrations(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }

    // ==================== 群聊管理接口 ====================

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<Page<Message>>> getGroupList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> groups = adminGroupService.getGroupList(pageable);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PutMapping("/groups/{id}/owner")
    public ResponseEntity<ApiResponse<Void>> changeGroupOwner(
            @PathVariable Long id,
            @Valid @RequestBody ChangeGroupOwnerRequest request) {
        adminGroupService.changeOwner(id, request.getNewOwnerId());
        return ResponseEntity.ok(ApiResponse.success("群主更换成功", null));
    }

    @DeleteMapping("/groups/{id}")
    public ResponseEntity<ApiResponse<Void>> dissolveGroup(@PathVariable Long id) {
        adminGroupService.dissolveGroup(id);
        return ResponseEntity.ok(ApiResponse.success("群聊解散成功", null));
    }
}
