# 阶段三社交功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现关注/粉丝、私信、群聊、SSE实时推送等社交功能

**Architecture:**
- 采用标准三层架构（Controller -> Service -> Repository）
- 复用现有项目模式：Lombok + JPA + Spring Data JPA
- SSE推送基于Spring EventListener机制实现
- 新增6张数据库表，遵循现有命名规范

**Tech Stack:** Spring Boot 4.0.5, Java 17, JPA, SSE

---

## 文件结构概览

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── entity/
│   ├── Follow.java          # 新增
│   ├── Friendship.java     # 新增
│   ├── UserPrivacy.java    # 新增
│   ├── Message.java        # 新增
│   ├── MessageRead.java    # 新增
│   └── GroupMember.java    # 新增
├── repository/
│   ├── FollowRepository.java        # 新增
│   ├── FriendshipRepository.java    # 新增
│   ├── UserPrivacyRepository.java   # 新增
│   ├── MessageRepository.java       # 新增
│   ├── MessageReadRepository.java   # 新增
│   └── GroupMemberRepository.java   # 新增
├── dto/request/
│   ├── MessageRequest.java          # 新增
│   ├── GroupRequest.java            # 新增
│   ├── PrivacySettingsRequest.java  # 新增
│   └── ReadMessageRequest.java      # 新增
├── dto/response/
│   ├── FollowStatusResponse.java     # 新增
│   ├── ConversationResponse.java    # 新增
│   ├── MessageResponse.java         # 新增
│   ├── GroupResponse.java           # 新增
│   ├── GroupMemberResponse.java     # 新增
│   ├── UserStatsResponse.java       # 新增
│   ├── FeedResponse.java            # 新增
│   └── UnreadCountResponse.java     # 新增
├── service/
│   ├── FollowService.java           # 新增
│   ├── MessageService.java          # 新增
│   ├── GroupService.java            # 新增
│   ├── SseService.java              # 新增
│   └── PrivacyService.java          # 新增
├── controller/
│   ├── FollowController.java        # 新增
│   ├── MessageController.java       # 新增
│   ├── GroupController.java         # 新增
│   └── SseController.java          # 新增
└── config/
    └── SecurityConfig.java         # 修改 - 添加新接口权限配置
```

---

## Task 1: 创建实体类 (Entity)

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/Follow.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/Friendship.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/UserPrivacy.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/Message.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/MessageRead.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/entity/GroupMember.java`

---

### Task 1.1: 创建 Follow 实体

- [ ] **Step 1: 创建 Follow.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/Follow.java
git commit -m "feat: add Follow entity for follow relationships"
```

---

### Task 1.2: 创建 Friendship 实体

- [ ] **Step 1: 创建 Friendship.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "friendship", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "friend_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/Friendship.java
git commit -m "feat: add Friendship entity for mutual follow relationships"
```

---

### Task 1.3: 创建 UserPrivacy 实体

- [ ] **Step 1: 创建 UserPrivacy.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_privacy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "follow_list_visible", nullable = false)
    private Boolean followListVisible = true;

    @Column(name = "follower_list_visible", nullable = false)
    private Boolean followerListVisible = true;

    @Column(name = "friends_only_receive", nullable = false)
    private Boolean friendsOnlyReceive = false;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/UserPrivacy.java
git commit -m "feat: add UserPrivacy entity for privacy settings"
```

---

### Task 1.4: 创建 Message 实体

- [ ] **Step 1: 创建 Message.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 20)
    private String type;  // "private" or "group"

    @Column(name = "target_id", nullable = false)
    private Long targetId;  // 接收者ID或群ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/Message.java
git commit -m "feat: add Message entity for private and group messages"
```

---

### Task 1.5: 创建 MessageRead 实体

- [ ] **Step 1: 创建 MessageRead.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_read", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"message_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/MessageRead.java
git commit -m "feat: add MessageRead entity for read status tracking"
```

---

### Task 1.6: 创建 GroupMember 实体

- [ ] **Step 1: 创建 GroupMember.java**

```java
package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_member", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";  // OWNER, ADMIN, MEMBER

    @Column(nullable = false)
    private Boolean banned = false;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/entity/GroupMember.java
git commit -m "feat: add GroupMember entity for group chat management"
```

---

## Task 2: 创建 Repository 接口

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/FollowRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/FriendshipRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/UserPrivacyRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/MessageRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/MessageReadRepository.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/repository/GroupMemberRepository.java`

---

### Task 2.1: 创建 FollowRepository

- [ ] **Step 1: 创建 FollowRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    Page<Follow> findByFolloweeId(Long followeeId, Pageable pageable);
    long countByFollowerId(Long followerId);
    long countByFolloweeId(Long followeeId);
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/FollowRepository.java
git commit -m "feat: add FollowRepository"
```

---

### Task 2.2: 创建 FriendshipRepository

- [ ] **Step 1: 创建 FriendshipRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    Page<Friendship> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/FriendshipRepository.java
git commit -m "feat: add FriendshipRepository"
```

---

### Task 2.3: 创建 UserPrivacyRepository

- [ ] **Step 1: 创建 UserPrivacyRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.UserPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPrivacyRepository extends JpaRepository<UserPrivacy, Long> {
    Optional<UserPrivacy> findByUserId(Long userId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/UserPrivacyRepository.java
git commit -m "feat: add UserPrivacyRepository"
```

---

### Task 2.4: 创建 MessageRepository

- [ ] **Step 1: 创建 MessageRepository.java**

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/MessageRepository.java
git commit -m "feat: add MessageRepository"
```

---

### Task 2.5: 创建 MessageReadRepository

- [ ] **Step 1: 创建 MessageReadRepository.java**

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/MessageReadRepository.java
git commit -m "feat: add MessageReadRepository"
```

---

### Task 2.6: 创建 GroupMemberRepository

- [ ] **Step 1: 创建 GroupMemberRepository.java**

```java
package com.volleyball.volleyballcommunitybackend.repository;

import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/GroupMemberRepository.java
git commit -m "feat: add GroupMemberRepository"
```

---

## Task 3: 创建 DTO 类

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/MessageRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/GroupRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/PrivacySettingsRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/ReadMessageRequest.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/FollowStatusResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/ConversationResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/MessageResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/GroupResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/GroupMemberResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UserStatsResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/FeedResponse.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UnreadCountResponse.java`

---

### Task 3.1: 创建请求 DTO

- [ ] **Step 1: 创建 MessageRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
```

- [ ] **Step 2: 创建 GroupRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class GroupRequest {
    @NotBlank(message = "群名称不能为空")
    private String name;

    private String description;

    @NotEmpty(message = "至少需要1个成员")
    private List<Long> memberIds;  // 除创建者外的成员ID列表
}
```

- [ ] **Step 3: 创建 PrivacySettingsRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;

@Data
public class PrivacySettingsRequest {
    private Boolean followListVisible;
    private Boolean followerListVisible;
    private Boolean friendsOnlyReceive;
}
```

- [ ] **Step 4: 创建 ReadMessageRequest.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;

@Data
public class ReadMessageRequest {
    private Long conversationWithUserId;  // 私聊时传入
    private Long groupId;                  // 群聊时传入
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/MessageRequest.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/GroupRequest.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/PrivacySettingsRequest.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/request/ReadMessageRequest.java
git commit -m "feat: add request DTOs for social features"
```

---

### Task 3.2: 创建响应 DTO

- [ ] **Step 1: 创建 FollowStatusResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowStatusResponse {
    private Boolean following;      // 我是否关注了对方
    private Boolean followedBy;     // 对方是否关注了我
    private Boolean mutualFollow;   // 是否互关
}
```

- [ ] **Step 2: 创建 ConversationResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private Long oderId;
    private String oderNickname;
    private String oderAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
```

- [ ] **Step 3: 创建 MessageResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String type;  // "private" or "group"
    private Long targetId;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
```

- [ ] **Step 4: 创建 GroupResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String type;  // "group"
    private Integer memberCount;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 5: 创建 GroupMemberResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberResponse {
    private Long userId;
    private String nickname;
    private String avatar;
    private String role;  // OWNER, ADMIN, MEMBER
    private Boolean banned;
    private LocalDateTime joinedAt;
}
```

- [ ] **Step 6: 创建 UserStatsResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsResponse {
    private Long followCount;
    private Long followerCount;
    private Long postCount;
    private Long friendCount;
}
```

- [ ] **Step 7: 创建 FeedResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedResponse {
    private Long postId;
    private String title;
    private UserResponse user;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 8: 创建 UnreadCountResponse.java**

```java
package com.volleyball.volleyballcommunitybackend.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnreadCountResponse {
    private Long totalUnread;
}
```

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/FollowStatusResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/ConversationResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/MessageResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/GroupResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/GroupMemberResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UserStatsResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/FeedResponse.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UnreadCountResponse.java
git commit -m "feat: add response DTOs for social features"
```

---

## Task 4: 创建 Service 层

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/FollowService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/MessageService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/GroupService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/SseService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/service/PrivacyService.java`

---

### Task 4.1: 创建 PrivacyService

- [ ] **Step 1: 创建 PrivacyService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.entity.UserPrivacy;
import com.volleyball.volleyballcommunitybackend.repository.UserPrivacyRepository;
import org.springframework.stereotype.Service;

@Service
public class PrivacyService {

    private final UserPrivacyRepository userPrivacyRepository;

    public PrivacyService(UserPrivacyRepository userPrivacyRepository) {
        this.userPrivacyRepository = userPrivacyRepository;
    }

    public UserPrivacy getOrCreatePrivacySettings(Long userId) {
        return userPrivacyRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPrivacy privacy = new UserPrivacy();
                    privacy.setUserId(userId);
                    privacy.setFollowListVisible(true);
                    privacy.setFollowerListVisible(true);
                    privacy.setFriendsOnlyReceive(false);
                    return userPrivacyRepository.save(privacy);
                });
    }

    public boolean isFollowListVisible(Long userId, Long viewerId) {
        if (userId.equals(viewerId)) {
            return true;
        }
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFollowListVisible();
    }

    public boolean isFollowerListVisible(Long userId, Long viewerId) {
        if (userId.equals(viewerId)) {
            return true;
        }
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFollowerListVisible();
    }

    public boolean isFriendsOnlyReceive(Long userId) {
        UserPrivacy privacy = getOrCreatePrivacySettings(userId);
        return privacy.getFriendsOnlyReceive();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/PrivacyService.java
git commit -m "feat: add PrivacyService for privacy settings"
```

---

### Task 4.2: 创建 FollowService

- [ ] **Step 1: 创建 FollowService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.response.FollowStatusResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.entity.Follow;
import com.volleyball.volleyballcommunitybackend.entity.Friendship;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PrivacyService privacyService;

    public FollowService(FollowRepository followRepository, FriendshipRepository friendshipRepository,
                         UserRepository userRepository, PostRepository postRepository,
                         PrivacyService privacyService) {
        this.followRepository = followRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.privacyService = privacyService;
    }

    @Transactional
    public void followUser(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new RuntimeException("已经关注了该用户");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        followRepository.save(follow);

        // 检查是否互关
        if (followRepository.existsByFollowerIdAndFolloweeId(followeeId, followerId)) {
            // 双向都创建friendship记录
            if (!friendshipRepository.existsByUserIdAndFriendId(followerId, followeeId)) {
                Friendship f1 = new Friendship();
                f1.setUserId(followerId);
                f1.setFriendId(followeeId);
                friendshipRepository.save(f1);
            }
            if (!friendshipRepository.existsByUserIdAndFriendId(followeeId, followerId)) {
                Friendship f2 = new Friendship();
                f2.setUserId(followeeId);
                f2.setFriendId(followerId);
                friendshipRepository.save(f2);
            }
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
        friendshipRepository.deleteByUserIdAndFriendId(followerId, followeeId);
        friendshipRepository.deleteByUserIdAndFriendId(followeeId, followerId);
    }

    public FollowStatusResponse getFollowStatus(Long userId, Long targetUserId) {
        boolean following = followRepository.existsByFollowerIdAndFolloweeId(userId, targetUserId);
        boolean followedBy = followRepository.existsByFollowerIdAndFolloweeId(targetUserId, userId);
        boolean mutualFollow = following && followedBy;
        return new FollowStatusResponse(following, followedBy, mutualFollow);
    }

    public Page<UserResponse> getFollowingList(Long userId, Long viewerId, Pageable pageable) {
        if (!privacyService.isFollowListVisible(userId, viewerId)) {
            return Page.empty();
        }
        return followRepository.findByFollowerId(userId, pageable)
                .map(f -> toUserResponse(f.getFolloweeId()));
    }

    public Page<UserResponse> getFollowerList(Long userId, Long viewerId, Pageable pageable) {
        if (!privacyService.isFollowerListVisible(userId, viewerId)) {
            return Page.empty();
        }
        return followRepository.findByFolloweeId(userId, pageable)
                .map(f -> toUserResponse(f.getFollowerId()));
    }

    public Page<UserResponse> getFriendsList(Long userId, Pageable pageable) {
        return friendshipRepository.findByUserId(userId, pageable)
                .map(f -> toUserResponse(f.getFriendId()));
    }

    public long getFollowCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    public long getFollowerCount(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    public long getFriendCount(Long userId) {
        return friendshipRepository.countByUserId(userId);
    }

    private UserResponse toUserResponse(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/FollowService.java
git commit -m "feat: add FollowService for follow/fans functionality"
```

---

### Task 4.3: 创建 SseService

- [ ] **Step 1: 创建 SseService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void sendMessageToUser(Long userId, String eventType, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventType)
                            .data(data));
                } catch (IOException e) {
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/SseService.java
git commit -m "feat: add SseService for real-time message push"
```

---

### Task 4.4: 创建 MessageService

- [ ] **Step 1: 创建 MessageService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReadMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UnreadCountResponse;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final SseService sseService;
    private final PrivacyService privacyService;

    public MessageService(MessageRepository messageRepository, MessageReadRepository messageReadRepository,
                          UserRepository userRepository, FollowRepository followRepository,
                          SseService sseService, PrivacyService privacyService) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.sseService = sseService;
        this.privacyService = privacyService;
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, Long receiverId, MessageRequest request) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发消息");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查接收者是否设置"仅好友接收"
        if (privacyService.isFriendsOnlyReceive(receiverId)) {
            boolean isFriend = followRepository.existsByFollowerIdAndFolloweeId(senderId, receiverId)
                    && followRepository.existsByFollowerIdAndFolloweeId(receiverId, senderId);
            if (!isFriend) {
                throw new RuntimeException("对方只接收好友消息");
            }
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setType("private");
        message.setTargetId(receiverId);
        message.setContent(request.getContent());
        Message saved = messageRepository.save(message);

        // 发送者已读
        MessageRead senderRead = new MessageRead();
        senderRead.setMessageId(saved.getId());
        senderRead.setUserId(senderId);
        senderRead.setReadAt(LocalDateTime.now());
        messageReadRepository.save(senderRead);

        // 接收者未读
        MessageRead receiverRead = new MessageRead();
        receiverRead.setMessageId(saved.getId());
        receiverRead.setUserId(receiverId);
        messageReadRepository.save(receiverRead);

        MessageResponse response = toMessageResponse(saved);

        // SSE推送
        sseService.sendMessageToUser(receiverId, "newMessage", response);

        return response;
    }

    public Page<ConversationResponse> getConversations(Long userId, Pageable pageable) {
        Page<Long> conversationUserIds = messageRepository.findPrivateConversationUserIds(userId, pageable);

        return conversationUserIds.map(otherUserId -> {
            User otherUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Page<Message> lastMessages = messageRepository.findPrivateMessages(userId, otherUserId, Pageable.unpaged());
            Message lastMessage = lastMessages.hasContent() ? lastMessages.getContent().get(0) : null;

            long unreadCount = messageReadRepository.countByUserIdAndReadAtIsNull(userId);

            ConversationResponse conversation = new ConversationResponse();
            conversation.setOderId(otherUser.getId());
            conversation.setOderNickname(otherUser.getNickname());
            conversation.setOderAvatar(otherUser.getAvatar());
            conversation.setLastMessage(lastMessage != null ? lastMessage.getContent() : "");
            conversation.setLastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null);
            conversation.setUnreadCount((int) unreadCount);
            return conversation;
        });
    }

    public Page<MessageResponse> getPrivateMessages(Long userId, Long otherUserId, Pageable pageable) {
        return messageRepository.findPrivateMessages(userId, otherUserId, pageable)
                .map(this::toMessageResponse);
    }

    @Transactional
    public void markAsRead(Long userId, ReadMessageRequest request) {
        if (request.getConversationWithUserId() != null) {
            Long otherUserId = request.getConversationWithUserId();
            Page<Message> messages = messageRepository.findPrivateMessages(userId, otherUserId, Pageable.unpaged());
            List<Long> unreadMessageIds = messages.getContent().stream()
                    .filter(m -> messageReadRepository.findByMessageIdAndUserId(m.getId(), userId)
                            .map(r -> r.getReadAt() == null)
                            .orElse(false))
                    .map(Message::getId)
                    .collect(Collectors.toList());

            if (!unreadMessageIds.isEmpty()) {
                messageReadRepository.batchMarkAsRead(userId, unreadMessageIds, LocalDateTime.now());
            }
        } else if (request.getGroupId() != null) {
            // 群聊已读逻辑类似
        }
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = messageReadRepository.countByUserIdAndReadAtIsNull(userId);
        return new UnreadCountResponse(count);
    }

    private MessageResponse toMessageResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Boolean isRead = messageReadRepository.findByMessageIdAndUserId(message.getId(), message.getTargetId())
                .map(r -> r.getReadAt() != null)
                .orElse(true);

        return new MessageResponse(
                message.getId(),
                sender.getId(),
                sender.getNickname(),
                sender.getAvatar(),
                message.getType(),
                message.getTargetId(),
                message.getContent(),
                message.getCreatedAt(),
                isRead
        );
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/MessageService.java
git commit -m "feat: add MessageService for private messaging"
```

---

### Task 4.5: 创建 GroupService

- [ ] **Step 1: 创建 GroupService.java**

```java
package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.dto.request.GroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupMemberResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.entity.GroupMember;
import com.volleyball.volleyballcommunitybackend.entity.Message;
import com.volleyball.volleyballcommunitybackend.entity.MessageRead;
import com.volleyball.volleyballcommunitybackend.entity.User;
import com.volleyball.volleyballcommunitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    public GroupService(MessageRepository messageRepository, MessageReadRepository messageReadRepository,
                        GroupMemberRepository groupMemberRepository, UserRepository userRepository,
                        SseService sseService) {
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    @Transactional
    public GroupResponse createGroup(Long creatorId, GroupRequest request) {
        // 创建群聊会话
        Message groupMessage = new Message();
        groupMessage.setSenderId(creatorId);
        groupMessage.setType("group");
        groupMessage.setContent("群聊创建");
        groupMessage.setTargetId(0L); // 临时占位
        Message saved = messageRepository.save(groupMessage);

        Long groupId = saved.getId();

        // 更新群ID（因为我们用message id作为group id）
        groupMessage.setTargetId(groupId);
        messageRepository.save(groupMessage);

        // 创建者加群
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(creatorId);
        ownerMember.setRole("OWNER");
        groupMemberRepository.save(ownerMember);

        // 添加其他成员
        for (Long memberId : request.getMemberIds()) {
            if (!memberId.equals(creatorId)) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(memberId);
                member.setRole("MEMBER");
                groupMemberRepository.save(member);
            }
        }

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(request.getName());
        response.setDescription(request.getDescription());
        response.setType("group");
        response.setMemberCount(request.getMemberIds().size() + 1);
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    public GroupResponse getGroupInfo(Long groupId) {
        Message groupMessage = messageRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群聊不存在"));

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        GroupResponse response = new GroupResponse();
        response.setId(groupId);
        response.setName(groupMessage.getContent()); // 复用content存群名
        response.setDescription("");
        response.setType("group");
        response.setMemberCount((int) memberCount);
        response.setCreatedAt(groupMessage.getCreatedAt());
        return response;
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(this::toGroupMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以邀请成员");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new RuntimeException("该用户已在群中");
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroupId(groupId);
        newMember.setUserId(targetUserId);
        newMember.setRole("MEMBER");
        groupMemberRepository.save(newMember);
    }

    @Transactional
    public void removeMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole())) {
            throw new RuntimeException("只有群主可以移除成员");
        }

        if (operatorId.equals(targetUserId)) {
            throw new RuntimeException("群主不能被移除");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if ("OWNER".equals(member.getRole())) {
            throw new RuntimeException("群主不能退群，请先转让群或解散群");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void banMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以禁言");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        target.setBanned(true);
        groupMemberRepository.save(target);
    }

    @Transactional
    public void unbanMember(Long operatorId, Long groupId, Long targetUserId) {
        GroupMember operator = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (!"OWNER".equals(operator.getRole()) && !"ADMIN".equals(operator.getRole())) {
            throw new RuntimeException("只有群主和管理员可以解除禁言");
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("该用户不在群中"));

        target.setBanned(false);
        groupMemberRepository.save(target);
    }

    @Transactional
    public MessageResponse sendGroupMessage(Long senderId, Long groupId, MessageRequest request) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, senderId)
                .orElseThrow(() -> new RuntimeException("你不是群成员"));

        if (member.getBanned()) {
            throw new RuntimeException("你已被禁言");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setType("group");
        message.setTargetId(groupId);
        message.setContent(request.getContent());
        Message saved = messageRepository.save(message);

        // 所有成员（包括发送者）标记为已读
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        for (GroupMember m : members) {
            MessageRead read = new MessageRead();
            read.setMessageId(saved.getId());
            read.setUserId(m.getUserId());
            read.setReadAt(LocalDateTime.now()); // 发送者即已读
            messageReadRepository.save(read);
        }

        MessageResponse response = toMessageResponse(saved);

        // 推送给所有在线群成员
        for (GroupMember m : members) {
            if (!m.getUserId().equals(senderId)) {
                sseService.sendMessageToUser(m.getUserId(), "newGroupMessage", response);
            }
        }

        return response;
    }

    public Page<MessageResponse> getGroupMessages(Long groupId, Pageable pageable) {
        return messageRepository.findByTypeAndTargetIdOrderByCreatedAtDesc("group", groupId, pageable)
                .map(this::toMessageResponse);
    }

    private GroupMemberResponse toGroupMemberResponse(GroupMember member) {
        User user = userRepository.findById(member.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        GroupMemberResponse response = new GroupMemberResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRole(member.getRole());
        response.setBanned(member.getBanned());
        response.setJoinedAt(member.getJoinedAt());
        return response;
    }

    private MessageResponse toMessageResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return new MessageResponse(
                message.getId(),
                sender.getId(),
                sender.getNickname(),
                sender.getAvatar(),
                message.getType(),
                message.getTargetId(),
                message.getContent(),
                message.getCreatedAt(),
                true
        );
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/GroupService.java
git commit -m "feat: add GroupService for group chat functionality"
```

---

## Task 5: 创建 Controller 层

**Files:**
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/FollowController.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/MessageController.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/GroupController.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/SseController.java`

---

### Task 5.1: 创建 FollowController

- [ ] **Step 1: 创建 FollowController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.FollowStatusResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UserResponse;
import com.volleyball.volleyballcommunitybackend.service.FollowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/api/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        followService.followUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("关注成功", null));
    }

    @DeleteMapping("/api/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        followService.unfollowUser(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success("取消关注成功", null));
    }

    @GetMapping("/api/follow/{userId}/status")
    public ResponseEntity<ApiResponse<FollowStatusResponse>> getFollowStatus(
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        FollowStatusResponse status = followService.getFollowStatus(currentUserId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/api/user/{userId}/following")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFollowingList(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<UserResponse> list = followService.getFollowingList(userId, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/api/user/{userId}/followers")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFollowerList(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<UserResponse> list = followService.getFollowerList(userId, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/api/user/{userId}/friends")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getFriendsList(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<UserResponse> list = followService.getFriendsList(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/FollowController.java
git commit -m "feat: add FollowController for follow endpoints"
```

---

### Task 5.2: 创建 MessageController

- [ ] **Step 1: 创建 MessageController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.ReadMessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.ConversationResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.UnreadCountResponse;
import com.volleyball.volleyballcommunitybackend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/api/message/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
            Authentication authentication,
            Pageable pageable) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<ConversationResponse> conversations = messageService.getConversations(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/api/message/private/{userId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getPrivateMessages(
            @PathVariable Long userId,
            Authentication authentication,
            Pageable pageable) {
        Long currentUserId = (Long) authentication.getPrincipal();
        Page<MessageResponse> messages = messageService.getPrivateMessages(currentUserId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/message/private/{userId}")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long userId,
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        MessageResponse message = messageService.sendMessage(currentUserId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("发送成功", message));
    }

    @PostMapping("/api/message/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestBody ReadMessageRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        messageService.markAsRead(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("标记已读", null));
    }

    @GetMapping("/api/message/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        UnreadCountResponse count = messageService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/MessageController.java
git commit -m "feat: add MessageController for private messaging endpoints"
```

---

### Task 5.3: 创建 GroupController

- [ ] **Step 1: 创建 GroupController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.request.GroupRequest;
import com.volleyball.volleyballcommunitybackend.dto.request.MessageRequest;
import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupMemberResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.GroupResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.MessageResponse;
import com.volleyball.volleyballcommunitybackend.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/api/group")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody GroupRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        GroupResponse group = groupService.createGroup(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", group));
    }

    @GetMapping("/api/group/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupInfo(@PathVariable Long id) {
        GroupResponse group = groupService.getGroupInfo(id);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    @GetMapping("/api/group/{id}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(@PathVariable Long id) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(id);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/api/group/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.addMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("添加成功", null));
    }

    @DeleteMapping("/api/group/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.removeMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("移除成功", null));
    }

    @PostMapping("/api/group/{id}/members/{userId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable Long id,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.leaveGroup(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.success("退群成功", null));
    }

    @PostMapping("/api/group/{id}/ban/{userId}")
    public ResponseEntity<ApiResponse<Void>> banMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.banMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("禁言成功", null));
    }

    @DeleteMapping("/api/group/{id}/unban/{userId}")
    public ResponseEntity<ApiResponse<Void>> unbanMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        groupService.unbanMember(currentUserId, id, userId);
        return ResponseEntity.ok(ApiResponse.success("解除禁言成功", null));
    }

    @GetMapping("/api/group/{id}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getGroupMessages(
            @PathVariable Long id,
            Pageable pageable) {
        Page<MessageResponse> messages = groupService.getGroupMessages(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/group/{id}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendGroupMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        MessageResponse message = groupService.sendGroupMessage(currentUserId, id, request);
        return ResponseEntity.ok(ApiResponse.success("发送成功", message));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/GroupController.java
git commit -m "feat: add GroupController for group chat endpoints"
```

---

### Task 5.4: 创建 SseController

- [ ] **Step 1: 创建 SseController.java**

```java
package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return sseService.connect(userId);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/SseController.java
git commit -m "feat: add SseController for SSE connection endpoint"
```

---

## Task 6: 更新 SecurityConfig

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java`

---

### Task 6.1: 更新 SecurityConfig 添加新接口权限

- [ ] **Step 1: 读取并更新 SecurityConfig.java**

在现有的 `authorizeHttpRequests` 中添加新接口的权限配置：

```java
// 在原有的 permitAll 规则后添加：
.requestMatchers("/api/follow/**").authenticated()      // 关注需要登录
.requestMatchers("/api/message/**").authenticated()     // 私信需要登录
.requestMatchers("/api/group/**").authenticated()        // 群聊需要登录
.requestMatchers("/api/sse/**").authenticated()         // SSE需要登录
.requestMatchers(HttpMethod.GET, "/api/user/*/following").permitAll()   // 关注列表公开
.requestMatchers(HttpMethod.GET, "/api/user/*/followers").permitAll()   // 粉丝列表公开
.requestMatchers(HttpMethod.GET, "/api/user/*/friends").permitAll()     // 好友列表公开
.requestMatchers(HttpMethod.GET, "/api/user/*/stats").permitAll()      // 用户统计公开
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java
git commit -m "feat: update SecurityConfig for social feature endpoints"
```

---

## Task 7: 更新 UserService - 添加用户统计和动态流

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/service/UserService.java`
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UserStatsResponse.java` (已在Task 3.2创建)
- Create: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/FeedResponse.java` (已在Task 3.2创建)

---

### Task 7.1: 更新 UserService 添加社交信息

- [ ] **Step 1: 添加用户统计和动态流方法到 UserService.java**

在 UserService 中添加以下方法：

```java
public UserStatsResponse getUserStats(Long userId) {
    long followCount = followService.getFollowCount(userId);
    long followerCount = followService.getFollowerCount(userId);
    long friendCount = followService.getFriendCount(userId);
    long postCount = postRepository.countByUserId(userId); // 需要在 PostRepository 添加此方法

    return new UserStatsResponse(followCount, followerCount, postCount, friendCount);
}

public Page<FeedResponse> getUserFeed(Long userId, Pageable pageable) {
    // 查询该用户关注的人的帖子
    Page<Follow> following = followRepository.findByFollowerId(userId, Pageable.unpaged());
    List<Long> followingIds = following.getContent().stream()
            .map(Follow::getFolloweeId)
            .collect(Collectors.toList());

    if (followingIds.isEmpty()) {
        return Page.empty();
    }

    // 使用自定义查询获取关注的人的帖子
    return postRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable)
            .map(post -> {
                FeedResponse feed = new FeedResponse();
                feed.setPostId(post.getId());
                feed.setTitle(post.getTitle());
                feed.setCreatedAt(post.getCreatedAt());
                User user = userRepository.findById(post.getUserId())
                        .orElseThrow(() -> new RuntimeException("用户不存在"));
                feed.setUser(new UserResponse(
                        user.getId(), user.getUsername(), user.getNickname(),
                        user.getAvatar(), user.getBio(), user.getCreatedAt()
                ));
                return feed;
            });
}
```

- [ ] **Step 2: 更新 PostRepository 添加必要方法**

```java
// 在 PostRepository 中添加
long countByUserId(Long userId);
Page<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/UserService.java
git add src/main/java/com/volleyball/volleyballcommunitybackend/repository/PostRepository.java
git commit -m "feat: update UserService with social stats and feed"
```

---

## Task 8: 更新 UserController - 添加统计和动态流接口

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/controller/UserController.java`

---

### Task 8.1: 更新 UserController

- [ ] **Step 1: 添加新接口到 UserController.java**

```java
@GetMapping("/{id}/stats")
public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(@PathVariable Long id) {
    UserStatsResponse stats = userService.getUserStats(id);
    return ResponseEntity.ok(ApiResponse.success(stats));
}

@GetMapping("/{id}/feed")
public ResponseEntity<ApiResponse<Page<FeedResponse>>> getUserFeed(
        @PathVariable Long id,
        Pageable pageable) {
    Page<FeedResponse> feed = userService.getUserFeed(id, pageable);
    return ResponseEntity.ok(ApiResponse.success(feed));
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/controller/UserController.java
git commit -m "feat: update UserController with stats and feed endpoints"
```

---

## Task 9: 更新 UserResponse - 添加社交信息

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UserResponse.java`

---

### Task 9.1: 更新 UserResponse

- [ ] **Step 1: 修改 UserResponse.java 添加 stats 字段**

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private LocalDateTime createdAt;

    // 新增社交信息字段
    private UserStatsResponse stats;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/dto/response/UserResponse.java
git commit -m "feat: add stats field to UserResponse"
```

---

## Task 10: 更新 AuthService - 新用户注册时创建隐私设置

**Files:**
- Modify: `src/main/java/com/volleyball/volleyballcommunitybackend/service/AuthService.java`

---

### Task 10.1: 更新 AuthService

- [ ] **Step 1: 修改 AuthService.java 在注册时创建隐私设置**

在 `register` 方法中，注册成功后调用 privacyService 创建默认隐私设置：

```java
@Transactional
public UserResponse register(RegisterRequest request) {
    // ... 现有逻辑 ...

    User saved = userRepository.save(user);

    // 创建默认隐私设置
    privacyService.getOrCreatePrivacySettings(saved.getId());

    return toUserResponse(saved);
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/volleyball/volleyballcommunitybackend/service/AuthService.java
git commit -m "feat: create privacy settings on user registration"
```

---

## Task 11: 更新 API 文档

**Files:**
- Modify: `docs/api.md`
- Modify: `docs/project.md`

---

### Task 11.1: 更新 api.md 添加新接口文档

- [ ] **Step 1: 在 api.md 末尾添加新模块文档**

```markdown
---

## 关注/粉丝模块 /api/follow

### 关注用户

```
POST /api/follow/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 被关注用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "关注成功",
  "data": null
}
```

**注意**：需要登录

---

### 取消关注

```
DELETE /api/follow/{userId}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "取消关注成功",
  "data": null
}
```

---

### 获取关注状态

```
GET /api/follow/{userId}/status
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "following": true,
    "followedBy": false,
    "mutualFollow": false
  }
}
```

---

### 获取用户关注列表

```
GET /api/user/{userId}/following
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

### 获取用户粉丝列表

```
GET /api/user/{userId}/followers
```

---

### 获取互关好友列表

```
GET /api/user/{userId}/friends
```

---

## 私信模块 /api/message

### 获取会话列表

```
GET /api/message/conversations
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "oderId": 2,
        "oderNickname": "用户2",
        "oderAvatar": "...",
        "lastMessage": "你好",
        "lastMessageTime": "2026-04-01T10:00:00",
        "unreadCount": 5
      }
    ]
  }
}
```

---

### 获取与用户的私聊消息

```
GET /api/message/private/{userId}
```

---

### 发送私信

```
POST /api/message/private/{userId}
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 消息内容 |

**返回数据**：

```json
{
  "code": 200,
  "message": "发送成功",
  "data": {
    "id": 1,
    "senderId": 1,
    "senderNickname": "用户1",
    "content": "你好",
    "createdAt": "2026-04-01T10:00:00",
    "isRead": false
  }
}
```

---

### 标记消息已读

```
POST /api/message/read
```

**请求数据**：

```json
{
  "conversationWithUserId": 2
}
```

---

### 获取未读消息数

```
GET /api/message/unread-count
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUnread": 10
  }
}
```

---

## 群聊模块 /api/group

### 创建群聊

```
POST /api/group
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 群名称 |
| description | string | 否 | 群描述 |
| memberIds | array | 是 | 成员ID列表 |

**返回数据**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "排球群",
    "description": "",
    "type": "group",
    "memberCount": 3,
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

---

### 获取群信息

```
GET /api/group/{id}
```

---

### 获取群成员列表

```
GET /api/group/{id}/members
```

---

### 添加群成员

```
POST /api/group/{id}/members?userId={userId}
```

---

### 移除群成员

```
DELETE /api/group/{id}/members/{userId}
```

---

### 退群

```
POST /api/group/{id}/members/{userId}/leave
```

---

### 禁言成员

```
POST /api/group/{id}/ban/{userId}
```

---

### 解除禁言

```
DELETE /api/group/{id}/unban/{userId}
```

---

### 获取群聊消息

```
GET /api/group/{id}/messages
```

---

### 发送群消息

```
POST /api/group/{id}/messages
```

**请求数据**：

```json
{
  "content": "大家好"
}
```

---

## SSE 实时推送 /api/sse

### 建立连接

```
GET /api/sse/connect
```

**返回**：SSE事件流，支持 `newMessage` 和 `newGroupMessage` 事件类型
```

- [ ] **Step 2: Commit**

```bash
git add docs/api.md
git commit -m "docs: update api.md with social feature endpoints"
```

---

### Task 11.2: 更新 project.md

- [ ] **Step 1: 更新 project.md 添加新表和新功能**

在数据库设计部分添加新表说明，在开发进度部分更新阶段三状态。

- [ ] **Step 2: Commit**

```bash
git add docs/project.md
git commit -m "docs: update project.md with phase 3 completion"
```

---

## Task 12: 最终提交和合并

- [ ] **Step 1: 运行测试确保没有破坏现有功能**

```bash
./mvnw test
```

- [ ] **Step 2: 推送到远程**

```bash
git push origin master
```

---

## 实现总结

| 任务 | 文件数 | 说明 |
|------|--------|------|
| Entity | 6 | Follow, Friendship, UserPrivacy, Message, MessageRead, GroupMember |
| Repository | 6 | 对应的6个Repository |
| DTO | 12 | 4个Request DTO, 8个Response DTO |
| Service | 5 | FollowService, MessageService, GroupService, SseService, PrivacyService |
| Controller | 4 | FollowController, MessageController, GroupController, SseController |
| Config更新 | 1 | SecurityConfig |
| 文档更新 | 2 | api.md, project.md |

**新增 API 接口约 20 个**
