package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import com.volleyball.volleyballcommunitybackend.repository.EventSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EventSubscriptionService {

    private final EventSubscriptionRepository subscriptionRepository;
    private final SseService sseService;

    public EventSubscriptionService(EventSubscriptionRepository subscriptionRepository, SseService sseService) {
        this.subscriptionRepository = subscriptionRepository;
        this.sseService = sseService;
    }

    @Transactional
    public void subscribe(Long eventId, Long userId) {
        if (subscriptionRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已订阅该赛事");
        }
        EventSubscription sub = new EventSubscription();
        sub.setEventId(eventId);
        sub.setUserId(userId);
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void unsubscribe(Long eventId, Long userId) {
        if (!subscriptionRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("未订阅该赛事");
        }
        subscriptionRepository.deleteByEventIdAndUserId(eventId, userId);
    }

    public boolean isSubscribed(Long eventId, Long userId) {
        return subscriptionRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public long getSubscriberCount(Long eventId) {
        return subscriptionRepository.countByEventId(eventId);
    }

    /**
     * 向赛事所有订阅者推送通知
     */
    public void notifySubscribers(Long eventId, String eventType, Map<String, Object> data) {
        List<EventSubscription> subscriptions = subscriptionRepository.findByEventId(eventId);
        for (EventSubscription sub : subscriptions) {
            try {
                sseService.sendMessageToUser(sub.getUserId(), eventType, data);
            } catch (Exception e) {
                // 推送失败不影响业务
            }
        }
    }
}
