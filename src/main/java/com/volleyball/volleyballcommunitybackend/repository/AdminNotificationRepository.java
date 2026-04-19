package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.AdminNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    // 查询用户的所有私信通知
    Page<AdminNotification> findByTargetUserIdAndTypeOrderBySentAtDesc(Long userId, String type, Pageable pageable);

    // 查询所有广播通知
    Page<AdminNotification> findByTypeOrderBySentAtDesc(String type, Pageable pageable);

    // 查询未读通知数量（私信+广播）
    @Query("SELECT COUNT(n) FROM AdminNotification n WHERE (n.targetUserId = :userId OR n.targetUserId IS NULL) AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
}
