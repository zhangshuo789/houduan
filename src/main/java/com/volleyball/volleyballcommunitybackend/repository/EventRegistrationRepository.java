package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);
    Page<EventRegistration> findByEventIdAndStatus(Long eventId, String status, Pageable pageable);
    Page<EventRegistration> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);
    long countByEventIdAndStatus(Long eventId, String status);
}
