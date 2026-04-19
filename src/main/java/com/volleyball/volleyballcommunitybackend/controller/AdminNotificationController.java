package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.SendNotificationRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.entity.AdminNotification;
import com.volleyball.volleyballcommunitybackend.service.AdminNotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notification")
public class AdminNotificationController {

    private final AdminNotificationService notificationService;

    public AdminNotificationController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 发送通知（仅管理员）
     * @param type BROADCAST-广播通知，PRIVATE-私信
     * @param persist 仅对BROADCAST有效，true-存入数据库+广播，false-仅广播不存
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestParam(defaultValue = "BROADCAST") String type,
            @RequestParam(defaultValue = "true") Boolean persist,
            @Valid @RequestBody SendNotificationRequest request) {

        request.setType(type);
        request.setPersist(persist);

        notificationService.sendNotification(request);

        return ResponseEntity.ok(ApiResponse.success("通知已发送", null));
    }

    /**
     * 获取当前用户的通知列表（用户端接口，所有登录用户可访问）
     * 包括私信和广播通知
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<AdminNotification>>> getNotifications(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = Pageable.ofSize(20);
        Page<AdminNotification> notifications = notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("已标记为已读", null));
    }
}
