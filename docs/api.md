# API 接口文档

> 本文档为前端 Agent 提供接口调用说明

---

## 统一响应格式

所有接口统一返回以下格式：

```json
{
  "code": 200,       // 状态码
  "message": "success",  // 提示信息
  "data": {}         // 返回数据（可能为null）
}
```

## 错误码说明

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误/校验失败 |
| 401 | 未登录或token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 认证模块 /api/auth

### 注册

```
POST /api/auth/register
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名，4-20字符，唯一 |
| password | string | 是 | 密码，6-20字符 |
| nickname | string | 是 | 昵称，2-20字符 |

**返回数据**：

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "排球小子"
  }
}
```

---

### 登录

```
POST /api/auth/login
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**返回数据**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "user1",
      "nickname": "排球小子",
      "avatar": "http://localhost:8080/api/file/1"
    }
  }
}
```

**注意**：登录成功后，后续请求需在 Header 中携带 `Authorization: Bearer <token>`（SSE 连接除外，见 SSE 章节）

---

## 用户模块 /api/user

### 获取用户信息

```
GET /api/user/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "排球小子",
    "avatar": "http://localhost:8080/api/file/1",
    "bio": "热爱排球",
    "createdAt": "2026-04-01T10:00:00",
    "stats": {
      "followCount": 100,
      "followerCount": 50,
      "postCount": 30,
      "friendCount": 20
    }
  }
}
```

---

### 更新个人信息

```
PUT /api/user/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| avatarFileId | long | 否 | 头像文件ID（上传文件后获取） |
| bio | string | 否 | 个人简介 |

**返回数据**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "排球达人",
    "avatar": "http://localhost:8080/api/file/1",
    "bio": "更热爱排球了",
    "createdAt": "2026-04-01T10:00:00",
    "stats": {...}
  }
}
```

**注意**：需要登录，只能修改自己的信息。avatar 字段返回完整 URL，可直接展示

---

### 获取用户统计

```
GET /api/user/{id}/stats
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "followCount": 100,
    "followerCount": 50,
    "postCount": 30,
    "friendCount": 20
  }
}
```

---

### 获取用户动态流

```
GET /api/user/{id}/feed
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "postId": 1,
        "title": "帖子标题",
        "user": {
          "id": 1,
          "username": "user1",
          "nickname": "排球小子",
          "avatar": "http://localhost:8080/api/file/1",
          "bio": "热爱排球",
          "createdAt": "2026-04-01T10:00:00"
        },
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

---

### 获取用户发帖列表

```
GET /api/user/{id}/posts
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "postId": 1,
        "title": "帖子标题",
        "user": {
          "id": 1,
          "username": "user1",
          "nickname": "排球小子",
          "avatar": "http://localhost:8080/api/file/1",
          "bio": "热爱排球",
          "createdAt": "2026-04-01T10:00:00"
        },
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

**说明**：返回该用户的发帖列表，按时间倒序排列

---

### 获取用户关注列表

```
GET /api/user/{userId}/following
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 2,
        "username": "user2",
        "nickname": "用户2",
        "avatar": "http://localhost:8080/api/file/2",
        "bio": "...",
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

**注意**：需要登录

---

### 获取用户粉丝列表

```
GET /api/user/{userId}/followers
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：同获取用户关注列表

**注意**：需要登录

---

### 获取互关好友列表

```
GET /api/user/{userId}/friends
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：同获取用户关注列表

**注意**：无需登录

---

## 关注模块 /api/follow

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

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 取消关注的用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "取消关注成功",
  "data": null
}
```

**注意**：需要登录

---

### 获取关注状态

```
GET /api/follow/{userId}/status
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 目标用户ID |

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

**注意**：需要登录

---

## 私信模块 /api/message

### 获取会话列表

```
GET /api/message/conversations
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

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
        "oderAvatar": "http://localhost:8080/api/file/2",
        "lastMessage": "你好",
        "lastMessageTime": "2026-04-01T10:00:00",
        "unreadCount": 5
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

**注意**：需要登录，按最新消息时间倒序排列

---

### 获取与用户的私聊消息

```
GET /api/message/private/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 对方用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "senderId": 1,
        "senderNickname": "用户1",
        "senderAvatar": "http://localhost:8080/api/file/1",
        "type": "private",
        "targetId": 2,
        "content": "你好",
        "createdAt": "2026-04-01T10:00:00",
        "isRead": true
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

**注意**：需要登录，返回当前用户与指定用户之间的所有私聊消息

---

### 发送私信

```
POST /api/message/private/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 接收消息的用户ID |

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
    "senderAvatar": "http://localhost:8080/api/file/1",
    "type": "private",
    "targetId": 2,
    "content": "你好",
    "createdAt": "2026-04-01T10:00:00",
    "isRead": true
  }
}
```

**注意**：需要登录，不能给自己发消息

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

或

```json
{
  "groupId": 1
}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "标记已读",
  "data": null
}
```

**注意**：需要登录，传入 conversationWithUserId 表示私聊，传入 groupId 表示群聊

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

**注意**：需要登录

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
| memberIds | array | 是 | 成员ID列表（不含创建者） |

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

**注意**：需要登录，创建者自动成为群主并包含在 memberCount 中

---

### 获取群信息

```
GET /api/group/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "排球群",
    "description": "",
    "type": "group",
    "memberCount": 5,
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

---

### 获取群成员列表

```
GET /api/group/{id}/members
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 1,
      "nickname": "用户1",
      "avatar": "http://localhost:8080/api/file/1",
      "role": "OWNER",
      "banned": false,
      "joinedAt": "2026-04-01T10:00:00"
    },
    {
      "userId": 2,
      "nickname": "用户2",
      "avatar": "http://localhost:8080/api/file/2",
      "role": "MEMBER",
      "banned": false,
      "joinedAt": "2026-04-01T10:05:00"
    }
  ]
}
```

**注意**：role 字段：OWNER（群主）、ADMIN（管理员）、MEMBER（普通成员）

---

### 添加群成员

```
POST /api/group/{id}/members?userId={userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

**查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 被添加的用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

**注意**：需要登录，仅群主和管理员可添加成员

---

### 移除群成员

```
DELETE /api/group/{id}/members/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |
| userId | long | 被移除的用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "移除成功",
  "data": null
}
```

**注意**：需要登录，仅群主可移除成员

---

### 退群

```
POST /api/group/{id}/members/{userId}/leave
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

**注意**：实际路径为 `/api/group/{id}/members/{userId}/leave`，userId 从 URL 获取但实际使用当前登录用户ID

**返回数据**：

```json
{
  "code": 200,
  "message": "退群成功",
  "data": null
}
```

**注意**：需要登录，群主不能退群

---

### 禁言成员

```
POST /api/group/{id}/ban/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |
| userId | long | 被禁言的用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "禁言成功",
  "data": null
}
```

**注意**：需要登录，仅群主和管理员可禁言成员

---

### 解除禁言

```
DELETE /api/group/{id}/unban/{userId}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |
| userId | long | 被解除禁言的用户ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "解除禁言成功",
  "data": null
}
```

**注意**：需要登录，仅群主和管理员可解除禁言

---

### 获取群聊消息

```
GET /api/group/{id}/messages
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "senderId": 1,
        "senderNickname": "用户1",
        "senderAvatar": "http://localhost:8080/api/file/1",
        "type": "group",
        "targetId": 1,
        "content": "大家好",
        "createdAt": "2026-04-01T10:00:00",
        "isRead": true
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

**注意**：需要登录

---

### 发送群消息

```
POST /api/group/{id}/messages
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 群聊ID |

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
    "senderAvatar": "http://localhost:8080/api/file/1",
    "type": "group",
    "targetId": 1,
    "content": "大家好",
    "createdAt": "2026-04-01T10:00:00",
    "isRead": true
  }
}
```

**注意**：需要登录，发送者默认标记为已读，被禁言的成员不能发送消息

---

## SSE 实时推送 /api/sse

### 建立连接

```
GET /api/sse/connect?token={jwtToken}
```

**查询参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT 登录Token |

**返回**：SSE 事件流（`text/event-stream`）

**支持的事件类型**：

| 事件名 | 说明 | data 内容 |
|--------|------|-----------|
| newMessage | 新私信 | MessageResponse 对象 |
| newGroupMessage | 新群消息 | MessageResponse 对象 |

**连接示例**：

```javascript
const eventSource = new EventSource(`http://localhost:8080/api/sse/connect?token=${jwtToken}`);

eventSource.addEventListener('newMessage', (event) => {
    const message = JSON.parse(event.data);
    console.log('收到私信:', message);
});

eventSource.addEventListener('newGroupMessage', (event) => {
    const message = JSON.parse(event.data);
    console.log('收到群消息:', message);
});
```

**注意**：EventSource 不支持自定义 Header，Token 必须通过 Query 参数传递

---

## 板块模块 /api/boards

### 获取所有板块

```
GET /api/boards
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "技术讨论",
      "description": "技战术分析、训练方法交流",
      "icon": "🏐"
    }
  ]
}
```

---

### 获取板块下的帖子列表

```
GET /api/boards/{id}/posts
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 板块ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "如何提高扣球高度？",
        "content": "我最近在练习扣球...",
        "user": {
          "id": 1,
          "username": "user1",
          "nickname": "排球小子",
          "avatar": "http://localhost:8080/api/file/1",
          "bio": "热爱排球",
          "createdAt": "2026-04-01T10:00:00"
        },
        "board": {
          "id": 1,
          "name": "技术讨论"
        },
        "likeCount": 100,
        "favoriteCount": 50,
        "commentCount": 30,
        "liked": null,
        "favorited": null,
        "createdAt": "2026-04-01T10:00:00",
        "updatedAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

**注意**：`liked` 和 `favorited` 字段：已登录返回 true/false，未登录返回 null

---

## 帖子模块 /api/post

### 发帖

```
POST /api/post
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 标题，5-100字符 |
| content | string | 是 | 内容，支持Markdown |
| boardId | long | 是 | 板块ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "发帖成功",
  "data": {
    "id": 1,
    "title": "如何提高扣球高度？",
    "content": "我最近在练习扣球...",
    "user": {...},
    "board": {...},
    "likeCount": 0,
    "favoriteCount": 0,
    "commentCount": 0,
    "liked": false,
    "favorited": false,
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

**注意**：需要登录

---

### 获取帖子详情

```
GET /api/post/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "如何提高扣球高度？",
    "content": "我最近在练习扣球...\n\n## 我的情况\n身高180，垂直弹跳...",
    "user": {
      "id": 1,
      "username": "user1",
      "nickname": "排球小子",
      "avatar": "http://localhost:8080/api/file/1",
      "bio": "热爱排球",
      "createdAt": "2026-04-01T10:00:00"
    },
    "board": {
      "id": 1,
      "name": "技术讨论"
    },
    "likeCount": 100,
    "favoriteCount": 50,
    "commentCount": 30,
    "liked": true,
    "favorited": false,
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00"
  }
}
```

---

### 编辑帖子

```
PUT /api/post/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 标题 |
| content | string | 是 | 内容 |

**返回数据**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "title": "如何提高扣球高度？（已更新）",
    "content": "更新后的内容...",
    "updatedAt": "2026-04-01T12:00:00"
  }
}
```

**注意**：需要登录，只能编辑自己的帖子

---

### 删除帖子

```
DELETE /api/post/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**注意**：需要登录，只能删除自己的帖子

---

## 评论模块 /api/comment

### 评论帖子

```
POST /api/post/{id}/comment
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 评论内容 |
| parentId | long | 否 | 父评论ID（为空表示一级评论） |

**返回数据**：

```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "id": 1,
    "content": "写得不错！",
    "user": {
      "id": 1,
      "username": "user1",
      "nickname": "排球小子",
      "avatar": "http://localhost:8080/api/file/1",
      "bio": "热爱排球",
      "createdAt": "2026-04-01T10:00:00"
    },
    "parentId": null,
    "createdAt": "2026-04-01T10:00:00",
    "replies": null
  }
}
```

**注意**：需要登录，未登录返回 401

---

### 获取帖子评论列表

```
GET /api/post/{id}/comments
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "content": "一级评论",
        "user": {
          "id": 1,
          "username": "user1",
          "nickname": "排球小子",
          "avatar": "http://localhost:8080/api/file/1",
          "bio": "热爱排球",
          "createdAt": "2026-04-01T10:00:00"
        },
        "parentId": null,
        "createdAt": "2026-04-01T10:00:00",
        "replies": [
          {
            "id": 2,
            "content": "二级评论（回复）",
            "user": {
              "id": 2,
              "username": "user2",
              "nickname": "用户2",
              "avatar": "http://localhost:8080/api/file/2",
              "bio": "...",
              "createdAt": "2026-04-01T10:00:00"
            },
            "parentId": 1,
            "createdAt": "2026-04-01T11:00:00",
            "replies": null
          }
        ]
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

---

### 删除评论

```
DELETE /api/comment/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 评论ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**注意**：需要登录，只能删除自己的评论，未登录返回 401

---

## 点赞模块 /api/like

### 点赞帖子

```
POST /api/post/{id}/like
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

**注意**：需要登录，未登录返回 401

---

### 取消点赞

```
DELETE /api/post/{id}/unlike
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

**注意**：需要登录

---

### 获取点赞状态

```
GET /api/post/{id}/likeStatus
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**注意**：已登录返回 true/false，未登录返回 null

---

## 收藏模块 /api/favorite

### 收藏帖子

```
POST /api/post/{id}/favorite
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "收藏成功",
  "data": null
}
```

**注意**：需要登录，未登录返回 401

---

### 取消收藏

```
DELETE /api/post/{id}/unfavorite
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "取消收藏成功",
  "data": null
}
```

**注意**：需要登录

---

### 获取收藏状态

```
GET /api/post/{id}/favoriteStatus
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 帖子ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**注意**：已登录返回 true/false，未登录返回 null

---

### 获取用户收藏列表

```
GET /api/user/{id}/favorites
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 用户ID |

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "如何提高扣球高度？",
        "content": "...",
        "user": {...},
        "board": {...},
        "likeCount": 100,
        "favoriteCount": 50,
        "commentCount": 30,
        "liked": true,
        "favorited": true,
        "createdAt": "2026-04-01T10:00:00",
        "updatedAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 20,
    "totalPages": 2,
    "number": 0,
    "size": 10
  }
}
```

---

## 文件服务 /api/file

### 上传文件

```
POST /api/file/upload
```

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件 |
| type | string | 是 | 文件类型（avatar/post_image） |

**返回数据**：

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "fileName": "avatar.png",
    "url": "http://localhost:8080/api/file/1",
    "fileSize": 102400,
    "contentType": "image/png"
  }
}
```

**注意**：需要登录，支持文件类型：avatar、post_image，最大 10MB

---

### 获取文件

```
GET /api/file/{id}
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**返回**：文件二进制流

**响应头**：

```
Content-Type: image/png
Content-Disposition: inline; filename="avatar.png"
```

---

### 获取文件完整URL

```
GET /api/file/{id}/url
```

**路径参数**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 文件ID |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": "http://localhost:8080/api/file/1"
}
```

---

## 赛事模块 /api/event

### 获取赛事列表

```
GET /api/event
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "2026春季排球联赛",
        "description": "...",
        "type": "MATCH",
        "status": "REGISTERING",
        "startTime": "2026-05-01T09:00:00",
        "endTime": "2026-05-03T18:00:00",
        "location": "国家体育馆",
        "organizer": "排球协会",
        "requirements": "身体健康，18岁以上",
        "maxParticipants": 16,
        "fee": 500.00,
        "contactInfo": "电话：xxx",
        "registrationDeadline": "2026-04-25T23:59:59",
        "imageUrls": ["http://xxx.com/1.jpg"],
        "createdBy": {
          "id": 1,
          "nickname": "管理员",
          "avatar": "http://xxx.com/avatar.png"
        },
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

**说明**：按 startTime 升序排列

---

### 获取赛事详情

```
GET /api/event/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "2026春季排球联赛",
    "description": "...",
    "type": "MATCH",
    "status": "REGISTERING",
    "startTime": "2026-05-01T09:00:00",
    "endTime": "2026-05-03T18:00:00",
    "location": "国家体育馆",
    "organizer": "排球协会",
    "requirements": "身体健康，18岁以上",
    "maxParticipants": 16,
    "fee": 500.00,
    "contactInfo": "电话：xxx",
    "registrationDeadline": "2026-04-25T23:59:59",
    "imageUrls": ["http://xxx.com/1.jpg"],
    "createdBy": {...},
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00",
    "registrationCount": 8,
    "subscriberCount": 50,
    "isSubscribed": true,
    "hasRegistered": false
  }
}
```

**说明**：`isSubscribed` 和 `hasRegistered` 字段：已登录返回 true/false，未登录返回 null

---

### 创建赛事

```
POST /api/event
```

**权限**：需要登录，type=ACTIVITY 时人人可发布，type=MATCH 时需管理员

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 赛事标题，5-100字符 |
| description | string | 是 | 赛事描述 |
| type | string | 是 | MATCH(比赛) 或 ACTIVITY(活动) |
| startTime | datetime | 是 | 开始时间 |
| endTime | datetime | 是 | 结束时间 |
| location | string | 是 | 地点 |
| organizer | string | 否 | 主办方 |
| requirements | string | 否 | 参赛要求 |
| maxParticipants | int | 否 | 最大参赛队伍数 |
| fee | decimal | 否 | 费用 |
| contactInfo | string | 否 | 联系方式 |
| registrationDeadline | datetime | 否 | 报名截止时间 |
| imageUrls | array | 否 | 图片URL列表 |

**返回数据**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {...}
}
```

---

### 更新赛事

```
PUT /api/event/{id}
```

**权限**：仅管理员或发布者可更新

---

### 删除赛事

```
DELETE /api/event/{id}
```

**权限**：仅管理员可删除

---

### 订阅赛事

```
POST /api/event/{id}/subscribe
```

**返回数据**：

```json
{
  "code": 200,
  "message": "订阅成功",
  "data": null
}
```

---

### 取消订阅

```
DELETE /api/event/{id}/subscribe
```

---

### 报名赛事

```
POST /api/event/{id}/register
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| teamName | string | 是 | 球队名称 |
| contactPerson | string | 是 | 联系人 |
| contactPhone | string | 是 | 联系方式 |
| teamSize | int | 是 | 参赛人数 |

**返回数据**：

```json
{
  "code": 200,
  "message": "报名成功，等待审核",
  "data": {
    "id": 1,
    "teamName": "冠军队",
    "contactPerson": "张三",
    "contactPhone": "138****8000",
    "teamSize": 12,
    "status": "PENDING",
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

---

### 查看报名列表

```
GET /api/event/{id}/registration
```

**权限**：仅管理员或发布者可查看

---

### 审核报名

```
PUT /api/event/{id}/registration/{regId}?approved=true
```

**权限**：仅管理员或发布者可审核

---

### 获取用户订阅列表

```
GET /api/user/{id}/subscriptions
```

**说明**：需要登录，仅本人可访问

---

## SSE 赛事事件

建立 SSE 连接后，会收到以下赛事相关事件：

| 事件名 | 触发时机 | data 内容 |
|--------|----------|-----------|
| eventUpdate | 赛事信息更新 | Event 对象 |
| eventStatusChanged | 赛事状态变更 | {eventId, oldStatus, newStatus} |
| newRegistration | 新报名（通知主办方） | Registration 对象 |
| registrationResult | 报名审核结果（通知报名者） | {eventId, eventTitle, approved} |

---

## 管理员模块 /api/admin

> **权限**：所有接口需 ADMIN 角色

### 用户管理

#### 获取用户列表

```
GET /api/admin/users
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |
| keyword | string | 否 | | 搜索关键词(用户名/昵称) |
| disabled | boolean | 否 | | 筛选禁用状态 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "avatar": "http://xxx.com/avatar.png",
        "bio": "系统管理员",
        "roles": ["ADMIN"],
        "disabled": false,
        "disabledReason": null,
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

#### 设置用户角色

```
PUT /api/admin/users/{id}/role
```

**请求数据**：

```json
{
  "roleId": 1
}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

#### 设置用户状态

```
PUT /api/admin/users/{id}/status
```

**请求数据**：

```json
{
  "disabled": true,
  "reason": "违规发言"
}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "设置成功",
  "data": null
}
```

---

### 内容审核

#### 提交举报

```
POST /api/admin/reports
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 被举报类型：POST/COMMENT/EVENT |
| targetId | long | 是 | 被举报内容ID |
| reason | string | 是 | 举报原因 |

**返回数据**：

```json
{
  "code": 200,
  "message": "举报成功",
  "data": null
}
```

#### 获取举报列表

```
GET /api/admin/reports
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |
| status | string | 否 | | 筛选状态：PENDING/HANDLED/REJECTED |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "reporterId": 2,
        "reporterNickname": "举报人",
        "targetType": "POST",
        "targetId": 10,
        "targetTitle": "帖子标题",
        "reason": "违规内容",
        "status": "PENDING",
        "handledBy": null,
        "handledByNickname": null,
        "handledAt": null,
        "handleResult": null,
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

#### 处理举报

```
PUT /api/admin/reports/{id}
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | boolean | 是 | true=确认举报(删除内容), false=驳回 |
| result | string | 否 | HANDLED时结果：CONTENT_DELETED/WARN_USER/DISMISS |

**返回数据**：

```json
{
  "code": 200,
  "message": "处理成功",
  "data": null
}
```

#### 获取待处理举报数量

```
GET /api/admin/reports/pending
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pendingCount": 5
  }
}
```

---

### 内容管理

#### 删除违规帖子

```
DELETE /api/admin/posts/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 删除违规评论

```
DELETE /api/admin/comments/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 赛事管理

#### 获取赛事列表

```
GET /api/admin/events
```

**查询参数**：同 `GET /api/event`

#### 修改赛事状态

```
PUT /api/admin/events/{id}/status
```

**请求数据**：

```json
{
  "status": "CANCELLED"
}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

#### 获取赛事报名列表

```
GET /api/admin/events/{id}/registrations
```

**说明**：仅管理员或发布者可查看

---

### 群聊管理

#### 获取群列表

```
GET /api/admin/groups
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

#### 更换群主

```
PUT /api/admin/groups/{id}/owner
```

**请求数据**：

```json
{
  "newOwnerId": 2
}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "更换成功",
  "data": null
}
```

#### 解散违规群

```
DELETE /api/admin/groups/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "解散成功",
  "data": null
}
```

---

### 数据统计

#### 运营概览

```
GET /api/admin/stats/overview
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 1000,
    "totalPosts": 5000,
    "totalComments": 10000,
    "totalEvents": 50,
    "totalGroups": 20,
    "totalReports": 10,
    "pendingReports": 3,
    "disabledUsers": 5
  }
}
```

#### 用户统计

```
GET /api/admin/stats/users
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 1000,
    "newUsersToday": 10,
    "newUsersThisMonth": 200,
    "activeUsersToday": 100,
    "activeUsersThisMonth": 500
  }
}
```

#### 内容统计

```
GET /api/admin/stats/content
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalPosts": 5000,
    "totalComments": 10000,
    "postsToday": 20,
    "commentsToday": 50,
    "postsThisMonth": 300,
    "commentsThisMonth": 800
  }
}
```

---

### 系统配置

#### 板块管理

```
GET /api/admin/boards
```

**返回数据**：同 `GET /api/boards`

```
POST /api/admin/boards
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 板块名称 |
| description | string | 否 | 板块描述 |
| icon | string | 否 | 板块图标 |

```
PUT /api/admin/boards/{id}
```

**请求数据**：同 POST

```
DELETE /api/admin/boards/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

#### 敏感词管理

##### 获取敏感词列表

```
GET /api/admin/sensitive-words
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "word": "违规词",
      "replacement": "***",
      "level": "WARN",
      "createdAt": "2026-04-01T10:00:00"
    }
  ]
}
```

##### 添加敏感词

```
POST /api/admin/sensitive-words
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| word | string | 是 | 敏感词 |
| replacement | string | 否 | 替换词，默认 `***` |
| level | string | 否 | 级别：WARN/BLOCK |

**返回数据**：

```json
{
  "code": 200,
  "message": "添加成功",
  "data": null
}
```

##### 更新敏感词

```
PUT /api/admin/sensitive-words/{id}
```

**请求数据**：同 POST

##### 删除敏感词

```
DELETE /api/admin/sensitive-words/{id}
```

---

### 操作日志

#### 获取操作日志

```
GET /api/admin/logs
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |