# API 接口文档

> 本文档为前端 Agent 提供接口调用说明

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
      "avatar": "https://example.com/avatar.png"
    }
  }
}
```

**注意**：登录成功后，后续请求需在 Header 中携带 `Authorization: Bearer <token>`

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
    "avatar": "https://example.com/avatar.png",
    "bio": "热爱排球",
    "createdAt": "2026-04-01T10:00:00"
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
| avatar | string | 否 | 头像URL |
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
    "avatar": "https://example.com/new-avatar.png",
    "bio": "更热爱排球了"
  }
}
```

**注意**：需要登录，只能修改自己的信息

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
    },
    {
      "id": 2,
      "name": "赛事资讯",
      "description": "国内外排球赛事报道",
      "icon": "🏆"
    },
    {
      "id": 3,
      "name": "装备评测",
      "description": "球鞋、球服、护具等装备测评",
      "icon": "👟"
    },
    {
      "id": 4,
      "name": "约球专区",
      "description": "组队约球、招募球员",
      "icon": "🤝"
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
          "nickname": "排球小子",
          "avatar": "https://example.com/avatar.png"
        },
        "board": {
          "id": 1,
          "name": "技术讨论"
        },
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
    "user": {
      "id": 1,
      "nickname": "排球小子",
      "avatar": "https://example.com/avatar.png"
    },
    "board": {
      "id": 1,
      "name": "技术讨论"
    },
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
      "nickname": "排球小子",
      "avatar": "https://example.com/avatar.png"
    },
    "board": {
      "id": 1,
      "name": "技术讨论"
    },
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
      "nickname": "排球小子",
      "avatar": "https://example.com/avatar.png"
    },
    "parentId": null,
    "createdAt": "2026-04-01T10:00:00",
    "replies": null
  }
}
```

**注意**：需要登录，未登录返回 401 "请先登录"

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
          "nickname": "排球小子",
          "avatar": "https://example.com/avatar.png"
        },
        "parentId": null,
        "createdAt": "2026-04-01T10:00:00",
        "replies": [
          {
            "id": 2,
            "content": "二级评论（回复）",
            "user": {
              "id": 2,
              "nickname": "用户2",
              "avatar": "https://example.com/avatar2.png"
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

**注意**：需要登录，只能删除自己的评论，未登录返回 401 "请先登录"

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

**注意**：需要登录，未登录返回 401 "请先登录"

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

**注意**：需要登录，未登录返回 401 "请先登录"

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

**注意**：需要登录，未登录返回 401 "请先登录"

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

**注意**：需要登录，未登录返回 401 "请先登录"

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
        "createdAt": "...",
        "updatedAt": "..."
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

## 帖子详情更新

### 获取帖子详情（已更新）

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
    "content": "我最近在练习扣球...",
    "user": {
      "id": 1,
      "nickname": "排球小子",
      "avatar": "https://example.com/avatar.png"
    },
    "board": {
      "id": 1,
      "name": "技术讨论"
    },
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00",
    "likeCount": 100,
    "favoriteCount": 50,
    "commentCount": 30,
    "liked": true,
    "favorited": false
  }
}
```

**注意**：`liked` 和 `favorited` 字段：已登录返回 true/false，未登录返回 null

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

**注意**：需要登录验证

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

**注意**：返回完整可访问的 URL，前端可直接展示

---

## 统一响应格式

所有接口统一返回以下格式：

```json
{
  "code": 200,       // 状态码：200成功，400参数错误，401未登录，403无权限，404不存在，500服务器错误
  "message": "success",  // 提示信息
  "data": {}         // 返回数据（可能为null）
}
```

---

## 错误码说明

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误/校验失败 |
| 401 | 未登录或token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
