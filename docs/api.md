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