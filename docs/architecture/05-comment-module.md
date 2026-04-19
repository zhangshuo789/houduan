# 05 评论模块

## 模块概述

评论（Comment）用于用户对帖子进行互动回复，支持嵌套回复（回复的回复）。评论发布时同样会经过敏感词过滤。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── CommentController.java           # 评论接口
├── service/
│   └── CommentService.java              # 评论业务逻辑
├── repository/
│   └── CommentRepository.java            # 评论数据访问
├── entity/
│   └── Comment.java                     # 评论实体
└── dto/request/
    └── CommentRequest.java               # 评论请求
```

## 数据表结构

**comment表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| content | TEXT | 评论内容 |
| user_id | BIGINT | 评论者ID |
| post_id | BIGINT | 所属帖子ID |
| parent_id | BIGINT | 父评论ID（null表示顶级评论） |
| created_at | DATETIME | 创建时间 |

## 代码流转

### 添加评论

```
POST /api/post/{postId}/comment
Authorization: Bearer <token>
    ↓
[CommentRequest] content, parentId (可选) 参数校验
    ↓
CommentService.addComment(userId, postId, request)
    ↓
PostRepository.existsById(postId) 验证帖子存在
    ↓
parentId != null → CommentRepository.existsById(parentId) 验证父评论存在
    ↓
SensitiveWordFilter.filter(content) 敏感词过滤
    ↓
new Comment() 构建评论实体
    ↓
CommentRepository.save(comment)
    ↓
更新帖子的评论数
    ↓
return CommentResponse
```

### 获取评论列表

```
GET /api/post/{postId}/comments?page=0&size=20
    ↓
CommentService.getComments(postId, pageable)
    ↓
CommentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
    ↓
CommentService.buildCommentResponses() 构建响应
    ↓
return Page<CommentResponse>
```

### 删除评论

```
DELETE /api/comment/{commentId}
Authorization: Bearer <token>
    ↓
CommentRepository.findById(commentId)
    ↓ 非评论者 → 抛出RuntimeException("无权限")
    ↓
级联删除子评论
    ↓
CommentRepository.delete(comment)
    ↓
更新帖子的评论数
    ↓
return ApiResponse.success("删除成功")
```

## 嵌套回复数据结构

评论支持无限层级嵌套，响应结构如下：

```json
{
  "id": 1,
  "content": "这是顶级评论",
  "user": { "id": 1, "nickname": "用户A", "avatar": "..." },
  "postId": 1,
  "parentId": null,
  "createdAt": "2026-04-19T10:00:00",
  "replies": [
    {
      "id": 2,
      "content": "这是回复",
      "user": { "id": 2, "nickname": "用户B" },
      "postId": 1,
      "parentId": 1,
      "createdAt": "2026-04-19T10:05:00",
      "replies": []
    }
  ]
}
```

## 接口详情

### POST /api/post/{postId}/comment 添加评论

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
  "content": "string (1-500字符)",
  "parentId": "number (可选，回复某条评论时传)"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "content": "评论内容",
    "user": { "id": 1, "nickname": "排球达人", "avatar": "..." },
    "postId": 1,
    "parentId": null,
    "createdAt": "2026-04-19T10:00:00"
  }
}
```

### GET /api/post/{postId}/comments 获取评论列表

**查询参数**: `page`, `size`

**响应**: 分页的评论列表（包含嵌套的replies）
