package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSubscriptionRepository extends JpaRepository<EventSubscription, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    void deleteByEventIdAndUserId(Long eventId, Long userId);
    Page<EventSubscription> findByUserId(Long userId, Pageable pageable);
    long countByEventId(Long eventId);
    List<EventSubscription> findByEventId(Long eventId);
}
