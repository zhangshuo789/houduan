package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.SendNotificationRequest;
import com.volleyball.volleyballcommunitybackend.entity.AdminNotification;
import com.volleyball.volleyballcommunitybackend.repository.AdminNotificationRepository;
import com.volleyball.volleyballcommunitybackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationService.class);

    private final AdminNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    public AdminNotificationService(AdminNotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   SseService sseService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    /**
     * 发送通知
     * @param request 通知请求
     */
    @Transactional
    public void sendNotification(SendNotificationRequest request) {
        Map<String, Object> data = buildNotificationData(request);

        if ("PRIVATE".equals(request.getType())) {
            // 私信模式：存入数据库 + SSE发送
            sendPrivateNotification(request, data);
        } else {
            // 广播模式
            if (Boolean.TRUE.equals(request.getPersist())) {
                // 存入数据库 + SSE广播
                sendPersistentBroadcast(request, data);
            } else {
                // 仅SSE广播，不存数据库
                sendEphemeralBroadcast(data);
            }
        }
    }

    /**
     * 私信：存入数据库并通过SSE发送
     */
    private void sendPrivateNotification(SendNotificationRequest request, Map<String, Object> data) {
        // 保存到数据库
        AdminNotification notification = new AdminNotification();
        notification.setType("PRIVATE");
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setTargetUserId(request.getTargetUserId());
        notification.setIsRead(false);
        notificationRepository.save(notification);

        // 通过SSE发送
        sseService.sendMessageToUser(request.getTargetUserId(), "adminNotification", data);
        log.info("私信已发送给用户: {}", request.getTargetUserId());
    }

    /**
     * 持久化广播：存入数据库并通过SSE广播
     */
    private void sendPersistentBroadcast(SendNotificationRequest request, Map<String, Object> data) {
        // 保存到数据库
        AdminNotification notification = new AdminNotification();
        notification.setType("BROADCAST");
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setTargetUserId(null);  // 广播无特定目标
        notification.setIsRead(false);
        notificationRepository.save(notification);

        // 通过SSE广播
        sseService.broadcast("adminNotification", data);
        log.info("持久化广播已发送: {}", request.getTitle());
    }

    /**
     * 临时广播：仅SSE广播，不存数据库
     */
    private void sendEphemeralBroadcast(Map<String, Object> data) {
        sseService.broadcast("adminNotification", data);
        log.info("临时广播已发送: {}", data.get("title"));
    }

    /**
     * 构建通知数据
     */
    private Map<String, Object> buildNotificationData(SendNotificationRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", System.currentTimeMillis());  // 临时ID
        data.put("type", request.getType());
        data.put("title", request.getTitle());
        data.put("content", request.getContent());
        data.put("sentAt", java.time.LocalDateTime.now().toString());
        return data;
    }

    /**
     * 获取用户未读通知数量（包括私信和广播）
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 获取用户通知列表（私信+广播）
     */
    public Page<AdminNotification> getUserNotifications(Long userId, Pageable pageable) {
        // 合并查询私信和广播通知
        org.springframework.data.domain.Page<AdminNotification> privateNotifications =
            notificationRepository.findByTargetUserIdAndTypeOrderBySentAtDesc(userId, "PRIVATE", pageable);
        org.springframework.data.domain.Page<AdminNotification> broadcastNotifications =
            notificationRepository.findByTypeOrderBySentAtDesc("BROADCAST", pageable);

        // 合并两个列表并按时间倒序
        java.util.List<AdminNotification> combined = new java.util.ArrayList<>();
        combined.addAll(privateNotifications.getContent());
        combined.addAll(broadcastNotifications.getContent());
        combined.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));

        // 分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), combined.size());
        java.util.List<AdminNotification> pageContent = start < combined.size()
            ? combined.subList(start, end)
            : java.util.Collections.emptyList();

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, combined.size());
    }

    /**
     * 获取用户私信列表
     */
    public Page<AdminNotification> getPrivateNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByTargetUserIdAndTypeOrderBySentAtDesc(userId, "PRIVATE", pageable);
    }

    /**
     * 获取广播通知列表
     */
    public Page<AdminNotification> getBroadcastNotifications(Pageable pageable) {
        return notificationRepository.findByTypeOrderBySentAtDesc("BROADCAST", pageable);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        AdminNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        // 只能标记自己的通知为已读（私信），广播通知任何人都可以标记
        if (notification.getTargetUserId() != null && !notification.getTargetUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
