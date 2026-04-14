package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {

    Optional<MessageRead> findByMessageIdAndUserId(Long messageId, Long userId);

    List<MessageRead> findByUserId(Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE MessageRead mr SET mr.readAt = :readAt WHERE mr.userId = :userId AND mr.messageId IN :messageIds AND mr.readAt IS NULL")
    int batchMarkAsRead(@Param("userId") Long userId, @Param("messageIds") List<Long> messageIds, @Param("readAt") LocalDateTime readAt);
}
