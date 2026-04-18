package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(String status);
    Optional<Report> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
