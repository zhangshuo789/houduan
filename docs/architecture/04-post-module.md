# 04 帖子模块

## 模块概述

帖子（Post）是社区的核心内容载体，用户可以在特定板块下发布帖子。帖子发布时会自动进行敏感词过滤，支持编辑和删除操作。

## 核心技术

| 技术 | 说明 |
|------|------|
| Spring Data JPA | 数据持久化 |
| 敏感词过滤 | 发布/编辑时自动过滤 |
| 分页查询 | 支持大量帖子的分页浏览 |

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── PostController.java              # 帖子接口
├── service/
│   └── PostService.java                 # 帖子业务逻辑
├── repository/
│   └── PostRepository.java              # 帖子数据访问
├── entity/
│   └── Post.java                        # 帖子实体
├── dto/request/
│   └── PostRequest.java                  # 创建/更新帖子请求
└── dto/response/
    ├── PostResponse.java                 # 帖子响应（含统计信息）
    └── PostDetailResponse.java           # 帖子详情响应
```

## 数据表结构

**post表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(100) | 帖子标题 |
| content | TEXT | 帖子内容 |
| user_id | BIGINT | 作者ID |
| board_id | BIGINT | 所属板块ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 代码流转

### 发布帖子

```
POST /api/post
Authorization: Bearer <token>
    ↓
[PostRequest] title, content, boardId 参数校验
    ↓
PostService.createPost(userId, request)
    ↓
BoardRepository.existsById(boardId) 验证板块存在
    ↓
SensitiveWordFilter.filter(content) 敏感词过滤
    ↓
new Post() 构建帖子实体
    ↓
PostRepository.save(post) 持久化
    ↓
return PostResponse（含author信息）
```

### 获取帖子详情

```
GET /api/post/{postId}
    ↓
PostService.getPostById(postId)
    ↓
PostRepository.findById(postId)
    ↓ 未找到 → 抛出RuntimeException("帖子不存在")
    ↓
PostRepository.incrementViewCount(postId) 原子递增浏览数
    ↓
PostService.buildPostDetailResponse(post) 构建详情
    ↓
return PostDetailResponse {
      id, title, content, author, board,
      likeCount, commentCount, isLiked, isFavorited,
      viewCount, createdAt, updatedAt
    }
```

### 更新帖子

```
PUT /api/post/{postId}
Authorization: Bearer <token>
    ↓
PostRepository.findById(postId)
    ↓ 非作者 → 抛出RuntimeException("无权限")
    ↓
SensitiveWordFilter.filter(newContent) 敏感词过滤
    ↓
更新 title, content
    ↓
PostRepository.save(post)
    ↓
return 更新后的PostResponse
```

### 删除帖子

```
DELETE /api/post/{postId}
Authorization: Bearer <token>
    ↓
PostRepository.findById(postId)
    ↓ 非作者且非管理员 → 抛出RuntimeException("无权限")
    ↓
级联删除: 删除帖子关联的评论、点赞、收藏
    ↓
PostRepository.delete(post)
    ↓
return ApiResponse.success("删除成功")
```

### 获取板块帖子列表

```
GET /api/post/board/{boardId}?page=0&size=10
    ↓
PostService.getPostsByBoardId(boardId, pageable)
    ↓
PostRepository.findByBoardIdOrderByCreatedAtDesc(boardId, pageable)
    ↓
PostService.enrichPostResponses() 批量补充:
    - 点赞数 (LikeRepository.countByPostId)
    - 评论数 (CommentRepository.countByPostId)
    - 当前用户是否点赞 (LikeRepository.existsByUserIdAndPostId)
    - 当前用户是否收藏 (FavoriteRepository.existsByUserIdAndPostId)
    ↓
return Page<PostResponse>
```

## 敏感词过滤时机

敏感词过滤在以下操作时自动执行：
1. **发布帖子** - `PostService.createPost()`
2. **更新帖子** - `PostService.updatePost()`

过滤规则参见 [15-敏感词过滤模块](15-sensitive-word-module.md)

## 接口详情

### POST /api/post 发布帖子

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
  "title": "string (1-100字符)",
  "content": "string (1-10000字符)",
  "boardId": "number"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "排球发球技巧分享",
    "content": "今天分享一个上手飘球的发球技巧...",
    "author": { "id": 1, "nickname": "排球达人", "avatar": "..." },
    "board": { "id": 1, "name": "技术交流" },
    "likeCount": 0,
    "commentCount": 0,
    "isLiked": false,
    "isFavorited": false,
    "createdAt": "2026-04-19T10:00:00"
  }
}
```

### GET /api/post/{postId} 获取帖子详情

**响应**: 包含完整帖子信息和统计数据的PostDetailResponse
