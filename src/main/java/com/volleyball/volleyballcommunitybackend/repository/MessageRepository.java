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
    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.targetId ELSE m.senderId END " +
           "FROM Message m WHERE m.type = 'private' AND (m.senderId = :userId OR m.targetId = :userId) " +
           "ORDER BY m.createdAt DESC")
    Page<Long> findPrivateConversationUserIds(@Param("userId") Long userId, Pageable pageable);
}
