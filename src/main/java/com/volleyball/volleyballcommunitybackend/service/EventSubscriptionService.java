package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import com.volleyball.volleyballcommunitybackend.repository.EventSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSubscriptionService {

    private final EventSubscriptionRepository eventSubscriptionRepository;

    public EventSubscriptionService(EventSubscriptionRepository eventSubscriptionRepository) {
        this.eventSubscriptionRepository = eventSubscriptionRepository;
    }

    @Transactional
    public void subscribe(Long eventId, Long userId) {
        if (eventSubscriptionRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("已订阅该赛事");
        }

        EventSubscription subscription = new EventSubscription();
        subscription.setEventId(eventId);
        subscription.setUserId(userId);
        eventSubscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(Long eventId, Long userId) {
        if (!eventSubscriptionRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new RuntimeException("未订阅该赛事");
        }

        eventSubscriptionRepository.deleteByEventIdAndUserId(eventId, userId);
    }
}
