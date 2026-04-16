package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 私聊消息查询（两人之间的所有消息）
    @Query("SELECT m FROM Message m WHERE m.type = 'private' AND " +
           "((m.senderId = :userId1 AND m.targetId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.targetId = :userId1)) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findPrivateMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    // 群聊消息查询
    Page<Message> findByTypeAndTargetIdOrderByCreatedAtDesc(String type, Long targetId, Pageable pageable);

    // 查询用户参与的所有私聊会话（按最新消息排序）
    // MySQL requires ORDER BY columns to be in SELECT when using DISTINCT, using native query
    @Query(value = "SELECT other_user_id FROM (" +
           "SELECT DISTINCT CASE WHEN sender_id = :userId THEN target_id ELSE sender_id END AS other_user_id " +
           "FROM message WHERE type = 'private' AND (sender_id = :userId OR target_id = :userId) " +
           "ORDER BY created_at DESC" +
           ") AS sub ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(DISTINCT CASE WHEN sender_id = :userId THEN target_id ELSE sender_id END) " +
           "FROM message WHERE type = 'private' AND (sender_id = :userId OR target_id = :userId)",
           nativeQuery = true)
    Page<Long> findPrivateConversationUserIds(@Param("userId") Long userId, Pageable pageable);
}
