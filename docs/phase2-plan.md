# 阶段二：互动功能

**目标**：评论、点赞、收藏

---

## 1. 数据库设计

### comment 评论表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| content | TEXT | 评论内容 |
| user_id | BIGINT | 评论用户ID |
| post_id | BIGINT | 被评论的帖子ID |
| parent_id | BIGINT | 父评论ID（为空表示一级评论） |
| created_at | DATETIME | 创建时间 |

### like 点赞表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| user_id | BIGINT | 点赞用户ID |
| post_id | BIGINT | 被点赞的帖子ID |
| created_at | DATETIME | 创建时间 |

**约束**：user_id + post_id 唯一，防止重复点赞

### favorite 收藏表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| user_id | BIGINT | 收藏用户ID |
| post_id | BIGINT | 被收藏的帖子ID |
| created_at | DATETIME | 创建时间 |

**约束**：user_id + post_id 唯一，防止重复收藏

---

## 2. API 接口设计

### 评论模块 /api/comment

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /post/{id}/comment | POST | 评论帖子 | 需登录 |
| /post/{id}/comments | GET | 获取评论列表 | 公开 |
| /comment/{id} | DELETE | 删除评论 | 需登录（本人） |

### 点赞模块 /api/like

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /post/{id}/like | POST | 点赞帖子 | 需登录 |
| /post/{id}/unlike | DELETE | 取消点赞 | 需登录 |
| /post/{id}/likeStatus | GET | 获取点赞状态 | 公开（已登录返回状态） |

### 收藏模块 /api/favorite

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /post/{id}/favorite | POST | 收藏帖子 | 需登录 |
| /post/{id}/unfavorite | DELETE | 取消收藏 | 需登录 |
| /post/{id}/favoriteStatus | GET | 获取收藏状态 | 公开（已登录返回状态） |
| /user/{id}/favorites | GET | 获取用户收藏列表 | 公开 |

---

## 3. 帖子详情返回内容

帖子详情 `GET /api/post/{id}` 新增返回字段：

```json
{
  "id": 1,
  "title": "...",
  "content": "...",
  "user": {...},
  "board": {...},
  "createdAt": "...",
  "updatedAt": "...",
  "likeCount": 100,
  "favoriteCount": 50,
  "commentCount": 30,
  "liked": false,       // 已登录用户是否点赞（未登录为null或省略）
  "favorited": false    // 已登录用户是否收藏（未登录为null或省略）
}
```

---

## 4. 评论列表返回

- 支持二级评论（parent_id 不为空表示回复）
- 分页：page=0, size=10
- 返回示例：

```json
{
  "content": [
    {
      "id": 1,
      "content": "一级评论",
      "user": {"id": 1, "nickname": "用户1", "avatar": "..."},
      "createdAt": "...",
      "replies": [
        {
          "id": 2,
          "content": "二级评论（回复）",
          "user": {"id": 2, "nickname": "用户2", "avatar": "..."},
          "parentId": 1,
          "createdAt": "..."
        }
      ]
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "number": 0,
  "size": 10
}
```

---

## 5. 未登录处理

- 评论/点赞/收藏接口：**未登录返回 401** + message "请先登录"
- 帖子详情：未登录时 `liked` 和 `favorited` 返回 `null`

---

## 6. 项目结构变更

新增：
```
├── entity/
│   ├── Comment.java        # 评论实体
│   ├── Like.java           # 点赞实体
│   └── Favorite.java       # 收藏实体
├── repository/
│   ├── CommentRepository.java
│   ├── LikeRepository.java
│   └── FavoriteRepository.java
├── service/
│   ├── CommentService.java
│   ├── LikeService.java
│   └── FavoriteService.java
├── controller/
│   ├── CommentController.java
│   ├── LikeController.java
│   └── FavoriteController.java
└── dto/
    ├── request/
    │   └── CommentRequest.java
    └── response/
        └── CommentResponse.java
```

---

## 7. SQL 变更

```sql
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `like` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
);

CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_post (user_id, post_id)
);
```

---

## 8. 任务清单

- [x] 创建实体类（Comment, Like, Favorite）
- [x] 创建 Repository
- [x] 创建 DTO
- [x] 实现 CommentService + CommentController
- [x] 实现 LikeService + LikeController
- [x] 实现 FavoriteService + FavoriteController
- [x] 更新 PostService（帖子返回点赞/收藏/评论数）
- [x] 更新 SecurityConfig（公开接口）
- [x] 创建 SQL 变更文件
- [x] 更新 api.md
- [x] 更新 project.md
