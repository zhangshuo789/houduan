package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
    Page<AdminLog> findByAdminIdOrderByCreatedAtDesc(Long adminId, Pageable pageable);
    Page<AdminLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<AdminLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
}
