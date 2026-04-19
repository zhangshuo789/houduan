# 09 私信消息模块

## 模块概述

私信（Message）模块实现用户之间的一对一私密通讯，支持消息发送、消息列表查询、已读标记等功能。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── MessageController.java           # 消息接口
├── service/
│   └── MessageService.java                # 消息业务逻辑
├── repository/
│   ├── MessageRepository.java             # 消息数据访问
│   └── MessageReadRepository.java          # 消息已读状态访问
└── entity/
    ├── Message.java                      # 消息实体
    └── MessageRead.java                  # 已读状态实体
```

## 数据表结构

**message表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| sender_id | BIGINT | 发送者ID |
| type | VARCHAR(20) | 消息类型: private/group |
| target_id | BIGINT | 接收者ID（私聊）或群ID（群聊） |
| content | TEXT | 消息内容 |
| created_at | DATETIME | 发送时间 |

**message_read表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| message_id | BIGINT | 消息ID |
| user_id | BIGINT | 已读用户ID |
| read_at | DATETIME | 已读时间 |

## 代码流转

### 发送私信

```
POST /api/message/private/{userId}
Authorization: Bearer <token>
    ↓
[MessageRequest] content 参数校验
    ↓
MessageService.sendPrivateMessage(senderId, userId, content)
    ↓
senderId == userId → 抛出RuntimeException("不能给自己发消息")
    ↓
检查接收者隐私设置: PrivacyService.isFriendsOnlyReceive(userId)
    ↓ 开启了"仅好友接收" → 检查是否双向关注
    ↓ 非好友 → 抛出RuntimeException("该用户只接收好友消息")
    ↓
new Message(senderId, PRIVATE, userId, content)
    ↓
MessageRepository.save(message)
    ↓
SseService.sendMessageToUser(userId, event) 实时推送
    ↓
return MessageResponse
```

### 获取会话列表

```
GET /api/message/conversations?page=0&size=20
Authorization: Bearer <token>
    ↓
MessageService.getConversations(userId, pageable)
    ↓
1. 查询用户发送的所有消息，按接收者分组
2. 查询用户接收的所有消息，按发送者分组
3. 合并去重，按最新消息时间排序
    ↓
返回会话列表，每个会话包含:
  - 对方用户信息
  - 最后一条消息
  - 未读消息数
```

### 获取与某用户的私信历史

```
GET /api/message/private/{userId}?page=0&size=50
Authorization: Bearer <token>
    ↓
MessageService.getPrivateMessages(currentUserId, userId, pageable)
    ↓
MessageRepository.findPrivateMessages(currentUserId, userId, pageable)
    ↓
按时间升序返回消息列表
    ↓
批量标记对方发送的消息为已读
    ↓
return Page<MessageResponse>
```

### 标记消息已读

```
POST /api/message/read
Authorization: Bearer <token>
    ↓
[MessageReadRequest] messageIds 参数
    ↓
MessageService.markAsRead(userId, messageIds)
    ↓
遍历messageIds:
  - MessageReadRepository.existsByMessageIdAndUserId
  - 不存在 → new MessageRead(messageId, userId, now)
  - MessageReadRepository.saveAll()
    ↓
return ApiResponse.success("已读标记成功")
```

### 获取未读消息数

```
GET /api/message/unread-count
Authorization: Bearer <token>
    ↓
MessageService.getUnreadCount(userId)
    ↓
MessageRepository.countUnreadMessages(userId)
    ↓
return { unreadCount: N }
```

## 实时推送

消息发送后通过SSE（参见 [11-SSE实时通知模块](11-sse-module.md)）实时推送给接收者：

```
MessageService.sendPrivateMessage()
    ↓
SseService.sendMessageToUser(receiverId, "message", newMessage)
    ↓
前端通过 EventSource 接收实时消息
```

## 接口详情

### POST /api/message/private/{userId} 发送私信

**请求体**:
```json
{
  "content": "string (1-2000字符)"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "你好！",
    "createdAt": "2026-04-19T10:00:00"
  }
}
```

### GET /api/message/conversations 获取会话列表

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "user": { "id": 2, "nickname": "用户B", "avatar": "..." },
      "lastMessage": { "content": "最后一条消息", "createdAt": "..." },
      "unreadCount": 3
    }
  ]
}
```

### GET /api/message/unread-count 获取未读数

**响应**:
```json
{
  "code": 200,
  "data": {
    "unreadCount": 5
  }
}
```
