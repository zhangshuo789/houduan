package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByOrderByStartTimeAsc(Pageable pageable);
    List<Event> findByStatusAndRegistrationDeadlineBefore(String status, LocalDateTime deadline);
    List<Event> findByStatusAndStartTimeBefore(String status, LocalDateTime startTime);
    List<Event> findByStatusAndEndTimeBefore(String status, LocalDateTime endTime);
}
