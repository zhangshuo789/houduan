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

    // 查询所有群聊（按创建时间倒序）
    Page<Message> findByTypeOrderByCreatedAtDesc(String type, Pageable pageable);

    // 查询用户参与的所有私聊会话（按最新消息排序）
    // 使用 GROUP BY 获取每个对话伙伴的最新消息时间
    @Query(value = "SELECT other_user_id FROM (" +
           "SELECT CASE WHEN sender_id = :userId THEN target_id ELSE sender_id END AS other_user_id, MAX(created_at) as latest_msg_time " +
           "FROM message WHERE type = 'private' AND (sender_id = :userId OR target_id = :userId) " +
           "GROUP BY other_user_id ORDER BY latest_msg_time DESC" +
           ") AS sub",
           countQuery = "SELECT COUNT(DISTINCT CASE WHEN sender_id = :userId THEN target_id ELSE sender_id END) " +
           "FROM message WHERE type = 'private' AND (sender_id = :userId OR target_id = :userId)",
           nativeQuery = true)
    Page<Long> findPrivateConversationUserIds(@Param("userId") Long userId, Pageable pageable);
}
