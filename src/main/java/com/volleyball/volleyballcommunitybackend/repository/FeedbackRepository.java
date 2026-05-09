package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Feedback> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    long countByStatus(String status);
}
