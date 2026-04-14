# 排球社区后端 - 阶段三社交功能设计文档

> 本文档描述阶段三（社交功能）的详细设计方案，包括关注/粉丝、私信、群聊等模块。

**创建日期**：2026-04-13
**状态**：待用户确认

---

## 一、功能概述

阶段三实现社交功能，包括：
- 关注/粉丝（双向关注）
- 私信（单聊 + 群聊）
- 实时消息推送（SSE）

---

## 二、数据库设计

### 2.1 新增表结构

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Follow    │     │  Friendship  │     │ Message     │
├─────────────┤     ├──────────────┤     ├─────────────┤
│ id (PK)     │     │ id (PK)      │     │ id (PK)     │
│ follower_id │────>│ user_id      │     │ sender_id   │
│ followee_id │     │ friend_id    │     │ type        │
│ created_at  │     │ created_at   │     │ (private/   │
└─────────────┘     └──────────────┘     │  group)     │
       │                                   │ target_id  │
       │                                   │ content    │
       ▼                                   │ created_at │
┌─────────────┐                            └─────────────┘
│UserPrivacy  │                                   │
├─────────────┤                            ┌──────┴──────┐
│ id (PK)     │                     ┌─────────────┐ ┌─────────────┐
│ user_id     │                     │MessageRead  │ │GroupMember  │
│ follow_     │                     ├─────────────┤ ├─────────────┤
│   list_      │                     │ id (PK)     │ │ id (PK)     │
│   visible    │                     │ message_id  │ │ group_id    │
│ follower_    │                     │ user_id     │ │ user_id     │
│   _list_      │                     │ read_at     │ │ role        │
│   visible    │                     └─────────────┘ │ joined_at   │
│ friends_     │                                       └─────────────┘
│   _only      │
│ receive_     │
│   _message   │
└─────────────┘
```

### 2.2 表说明

| 表名 | 说明 | 约束 |
|------|------|------|
| `follow` | 关注关系（单向） | (follower_id, followee_id) 唯一 |
| `friendship` | 互关关系（冗余存储） | (user_id, friend_id) 唯一 |
| `user_privacy` | 隐私设置 | user_id 唯一 |
| `message` | 消息 | type=private时target_id=接收者ID，type=group时target_id=群ID |
| `message_read` | 已读记录 | (message_id, user_id) 唯一 |
| `group_member` | 群成员 | (group_id, user_id) 唯一 |

### 2.3 DDL

```sql
-- 关注表
CREATE TABLE follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_follow (follower_id, followee_id),
    FOREIGN KEY (follower_id) REFERENCES user(id),
    FOREIGN KEY (followee_id) REFERENCES user(id)
);

-- 互关关系表
CREATE TABLE friendship (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_friendship (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (friend_id) REFERENCES user(id)
);

-- 用户隐私设置表
CREATE TABLE user_privacy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    follow_list_visible BOOLEAN DEFAULT TRUE,
    follower_list_visible BOOLEAN DEFAULT TRUE,
    friends_only_receive BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 消息表
CREATE TABLE message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'private/group',
    target_id BIGINT NOT NULL COMMENT '接收者ID或群ID',
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id)
);

-- 消息已读表
CREATE TABLE message_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    read_at DATETIME,
    UNIQUE KEY uk_message_read (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES message(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 群成员表
CREATE TABLE group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'OWNER/ADMIN/MEMBER',
    banned BOOLEAN DEFAULT FALSE,
    joined_at DATETIME NOT NULL,
    UNIQUE KEY uk_group_member (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES message(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

---

## 三、API 接口设计

### 3.1 关注/粉丝模块 `/api/follow`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/follow/{userId}` | POST | 关注用户 |
| `/api/follow/{userId}` | DELETE | 取消关注 |
| `/api/follow/{userId}/status` | GET | 获取关注状态（是否互关） |
| `/api/user/{userId}/following` | GET | 获取用户的关注列表 |
| `/api/user/{userId}/followers` | GET | 获取用户的粉丝列表 |
| `/api/user/{userId}/friends` | GET | 获取互关好友列表 |
| `/api/user/{userId}/stats` | GET | 获取用户社交统计 |

### 3.2 私信模块 `/api/message`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/message/conversations` | GET | 获取会话列表（包含未读数） |
| `/api/message/private/{userId}` | GET | 获取与某用户的私聊消息 |
| `/api/message/private/{userId}` | POST | 发送私信 |
| `/api/message/read` | POST | 标记消息已读 |
| `/api/message/unread-count` | GET | 获取全局未读消息数 |

### 3.3 群聊模块 `/api/group`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/group` | POST | 创建群聊 |
| `/api/group/{id}` | GET | 获取群信息 |
| `/api/group/{id}` | PUT | 修改群信息（仅群主） |
| `/api/group/{id}` | DELETE | 解散群聊（仅群主） |
| `/api/group/{id}/members` | GET | 获取群成员列表 |
| `/api/group/{id}/members` | POST | 邀请成员（仅群主/管理员） |
| `/api/group/{id}/members/{userId}` | DELETE | 移除成员（仅群主） |
| `/api/group/{id}/members/{userId}/leave` | POST | 主动退群 |
| `/api/group/{id}/ban/{userId}` | POST | 禁言成员（仅群主/管理员） |
| `/api/group/{id}/unban/{userId}` | DELETE | 解除禁言（仅群主/管理员） |
| `/api/group/{id}/messages` | GET | 获取群聊消息 |
| `/api/group/{id}/messages` | POST | 发送群消息 |

### 3.4 SSE 实时推送 `/api/sse`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/sse/connect` | GET | 建立 SSE 连接，订阅当前用户的消息 |

---

## 四、业务流程

### 4.1 关注/取消关注流程

```
关注用户:
1. 前端 POST /api/follow/{userId}
2. 后端校验：不能关注自己、不能重复关注
3. 创建 follow 记录
4. 检查是否互关（对方是否也关注了我）
   - 如果是，创建/更新 friendship 记录
5. 返回结果

取消关注:
1. 前端 DELETE /api/follow/{userId}
2. 删除 follow 记录
3. 删除 friendship 中对应的记录（如果有）
4. 返回结果
```

### 4.2 私信发送流程

```
发送私信:
1. 前端 POST /api/message/private/{userId}
2. 后端校验：
   - 发送者 ≠ 接收者
   - 接收者是否设置"仅好友接收"？
     - 是：检查是否互关，不是则返回 403
3. 创建 message 记录（type=private）
4. 创建 message_read 记录（初始为未读）
5. 通过 SSE 推送新消息给接收者
6. 返回消息详情
```

### 4.3 群聊创建流程

```
创建群聊:
1. 前端 POST /api/group（传入群名、成员列表）
2. 后端校验：至少2个成员（创建者+1人）
3. 创建 message 会话记录（type=group）
4. 创建 group_member 记录（创建者为群主 role=OWNER）
5. 将成员加入群
6. 返回群信息
```

### 4.4 SSE 消息推送流程

```
建立连接:
1. 前端 GET /api/sse/connect（携带 JWT）
2. 后端创建 SSE 连接，关联用户ID
3. 连接维持，断开时自动清理

推送消息:
1. 发送方调用消息接口
2. 消息入库后，查询接收者的 SSE 连接
3. 通过连接推送消息内容
4. 前端 EventSource 接收并更新 UI
```

---

## 五、隐私设置

### 5.1 隐私设置表（user_privacy）

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| follow_list_visible | BOOLEAN | true | 关注列表是否公开 |
| follower_list_visible | BOOLEAN | true | 粉丝列表是否公开 |
| friends_only_receive | BOOLEAN | false | 是否仅好友可发私信 |

**默认行为**：新用户注册时自动创建隐私设置（默认全部公开）

### 5.2 查询受保护的列表时的处理

```
获取用户关注列表 GET /api/user/{userId}/following:
1. 查询目标用户的隐私设置
2. 如果 follow_list_visible = false：
   - 如果查询者 ≠ 目标用户，返回空列表
   - 如果查询者 = 目标用户，返回完整列表
3. 如果 follow_list_visible = true：正常返回
```

---

## 六、消息已读机制

### 6.1 已读状态流程

```
发送消息时:
1. 创建 message 记录
2. 创建 message_read：user_id=发送者, read_at=当前时间（发送即已读）
3. 接收者的 message_read 记录 read_at = null（未读）

标记已读时 POST /api/message/read:
1. 前端传入 { conversationWithUserId: 123 } 或 { groupId: 456 }
2. 后端查询该会话所有 read_at = null 的消息
3. 批量更新 read_at = 当前时间
4. 返回更新数量

未读数计算:
unreadCount = count(message_read where user_id = 当前用户 AND read_at IS NULL)
```

---

## 七、群聊角色与权限

### 7.1 群成员角色

| 角色 | 值 | 权限 |
|------|-----|------|
| 群主 | OWNER | 管理群信息、邀请/踢除成员、禁言、解散群 |
| 管理员 | ADMIN | 邀请成员、禁言普通成员 |
| 成员 | MEMBER | 发送消息、主动退群 |

### 7.2 禁言机制

```
禁言用户:
1. 校验操作者权限（必须是群主或管理员）
2. 在 group_member 表设置 banned = true
3. 后续该用户发送消息返回 403 "您已被禁言"

发送群消息时:
1. 检查发送者是否是群成员
2. 检查是否被禁言，是则拒绝
```

---

## 八、用户主页社交信息

`GET /api/user/{userId}` 返回数据新增字段：

```json
{
  "id": 1,
  "username": "user1",
  "nickname": "排球小子",
  "avatar": "https://example.com/avatar.png",
  "bio": "热爱排球",
  "createdAt": "2026-04-01T10:00:00",
  "stats": {
    "followCount": 100,
    "followerCount": 50,
    "postCount": 30,
    "friendCount": 20
  },
  "recentFriends": [
    { "id": 2, "nickname": "好友1", "avatar": "..." },
    { "id": 3, "nickname": "好友2", "avatar": "..." }
  ],
  "feed": [
    {
      "postId": 1,
      "title": "帖子标题",
      "user": { "id": 2, "nickname": "好友1", "avatar": "..." },
      "createdAt": "2026-04-01T12:00:00"
    }
  ]
}
```

### 动态流（Feed）生成逻辑

```
获取用户动态:
1. 查询该用户关注的所有用户ID
2. 查询这些用户发布的最新 N 条帖子
3. 按时间倒序返回
4. 支持分页
```

---

## 九、实现方案总结

| 模块 | 功能 |
|------|------|
| 关注/粉丝 | 双向关注、互关好友、社交统计、隐私设置 |
| 私信 | 单聊、消息已读/未读、SSE实时推送 |
| 群聊 | 创建群聊、邀请/踢人、禁言管理、群消息 |
| 动态流 | 关注的人的帖子动态 |

**新增 API 约 20 个接口**
**新增 6 张数据库表**
