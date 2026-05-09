package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Page<EventRegistration> findByEventId(Long eventId, Pageable pageable);
    List<EventRegistration> findByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    long countByEventId(Long eventId);
    Optional<EventRegistration> findByEventIdAndBracketPosition(Long eventId, Integer bracketPosition);
    List<EventRegistration> findByEventIdAndEliminated(Long eventId, Boolean eliminated);
    Optional<EventRegistration> findByEventIdAndIsChampion(Long eventId, Boolean isChampion);
}
