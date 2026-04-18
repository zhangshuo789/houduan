package com.volleyball.volleyballcommunitybackend.scheduler;

import com.volleyball.volleyballcommunitybackend.entity.Event;
import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import com.volleyball.volleyballcommunitybackend.repository.EventRepository;
import com.volleyball.volleyballcommunitybackend.repository.EventSubscriptionRepository;
import com.volleyball.volleyballcommunitybackend.service.SseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    private final EventRepository eventRepository;
    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final SseService sseService;

    public EventStatusScheduler(EventRepository eventRepository,
                                EventSubscriptionRepository eventSubscriptionRepository,
                                SseService sseService) {
        this.eventRepository = eventRepository;
        this.eventSubscriptionRepository = eventSubscriptionRepository;
        this.sseService = sseService;
    }

    /**
     * 每分钟扫描一次赛事状态
     * - REGISTERING -> IN_PROGRESS: 到达赛事开始时间（报名已截止）
     * - IN_PROGRESS -> ENDED: 到达赛事结束时间
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行
    @Transactional
    public void monitorEventStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("赛事状态监控任务执行，当前时间: {}", now);

        // 1. REGISTERING -> IN_PROGRESS（报名已截止，赛事开始）
        processStatusChange(
            eventRepository.findByStatusAndStartTimeBefore("REGISTERING", now),
            "IN_PROGRESS",
            "赛事已开始，请准时参加"
        );

        // 2. IN_PROGRESS -> ENDED（赛事结束）
        processStatusChange(
            eventRepository.findByStatusAndEndTimeBefore("IN_PROGRESS", now),
            "ENDED",
            "赛事已结束，感谢参与"
        );
    }

    private void processStatusChange(List<Event> events, String newStatus, String notificationMsg) {
        for (Event event : events) {
            String oldStatus = event.getStatus();
            event.setStatus(newStatus);
            eventRepository.save(event);

            log.info("赛事状态变更: {} (ID: {}) 从 {} 变为 {}", event.getTitle(), event.getId(), oldStatus, newStatus);

            // 通知所有订阅者
            notifySubscribers(event, oldStatus, newStatus, notificationMsg);
        }
    }

    private void notifySubscribers(Event event, String oldStatus, String newStatus, String message) {
        // 获取所有订阅者
        List<EventSubscription> subscriptions = eventSubscriptionRepository.findByEventId(event.getId());
        List<Long> subscriberIds = subscriptions.stream()
                .map(EventSubscription::getUserId)
                .collect(Collectors.toList());

        if (subscriberIds.isEmpty()) {
            return;
        }

        // 构建通知数据
        Map<String, Object> notificationData = Map.of(
                "eventId", event.getId(),
                "eventTitle", event.getTitle(),
                "oldStatus", oldStatus,
                "newStatus", newStatus,
                "message", message
        );

        // 推送给每个订阅者
        for (Long userId : subscriberIds) {
            try {
                sseService.sendEventStatusChanged(userId, notificationData);
                log.debug("已推送赛事状态变更通知给用户: {}", userId);
            } catch (Exception e) {
                log.warn("推送赛事状态变更通知给用户失败: {}, error: {}", userId, e.getMessage());
            }
        }

        log.info("赛事状态变更通知已推送给 {} 个订阅者, 赛事: {}", subscriberIds.size(), event.getTitle());
    }
}
