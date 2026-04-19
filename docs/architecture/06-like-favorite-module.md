# 06 点赞收藏模块

## 模块概述

点赞（Like）和收藏（Favorite）是用户对帖子的两种互动方式。点赞表示认可，收藏表示稍后查看。两者都通过唯一约束防止重复操作。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   ├── LikeController.java              # 点赞接口
│   └── FavoriteController.java          # 收藏接口
├── service/
│   ├── LikeService.java                 # 点赞业务逻辑
│   └── FavoriteService.java             # 收藏业务逻辑
├── repository/
│   ├── LikeRepository.java              # 点赞数据访问
│   └── FavoriteRepository.java           # 收藏数据访问
└── entity/
    ├── Like.java                        # 点赞实体
    └── Favorite.java                   # 收藏实体
```

## 数据表结构

**like表** (注意：SQL中是反引号 `like`)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 点赞用户ID |
| post_id | BIGINT | 被点赞帖子ID |
| created_at | DATETIME | 点赞时间 |
| **UNIQUE** | | (user_id, post_id) 防止重复点赞 |

**favorite表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 收藏用户ID |
| post_id | BIGINT | 被收藏帖子ID |
| created_at | DATETIME | 收藏时间 |
| **UNIQUE** | | (user_id, post_id) 防止重复收藏 |

## 代码流转

### 点赞操作

```
POST /api/post/{postId}/like
Authorization: Bearer <token>
    ↓
LikeService.like(userId, postId)
    ↓
PostRepository.existsById(postId) 验证帖子存在
    ↓
LikeRepository.existsByUserIdAndPostId(userId, postId)
    ↓ 已点赞 → 抛出RuntimeException("已点赞")
    ↓ 未点赞
    ↓
new Like(userId, postId) 创建点赞记录
    ↓
LikeRepository.save(like)
    ↓
return ApiResponse.success("点赞成功")
```

### 取消点赞

```
DELETE /api/post/{postId}/unlike
Authorization: Bearer <token>
    ↓
LikeService.unlike(userId, postId)
    ↓
LikeRepository.deleteByUserIdAndPostId(userId, postId)
    ↓
return ApiResponse.success("取消点赞成功")
```

### 收藏操作

```
POST /api/post/{postId}/favorite
Authorization: Bearer <token>
    ↓
FavoriteService.favorite(userId, postId)
    ↓
检查是否已收藏 → 防止重复
    ↓
new Favorite(userId, postId)
    ↓
FavoriteRepository.save(favorite)
    ↓
return ApiResponse.success("收藏成功")
```

### 获取用户收藏列表

```
GET /api/user/{userId}/favorites?page=0&size=10
Authorization: Bearer <token>
    ↓
FavoriteService.getUserFavorites(userId, pageable)
    ↓
FavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
    ↓
PostService.enrichPostResponses() 补充帖子完整信息
    ↓
return Page<PostResponse>
```

### 批量查询点赞/收藏状态

```
GET /api/post/{postId}/likeStatus
GET /api/post/{postId}/favoriteStatus
Authorization: Bearer <token>
    ↓
LikeRepository.existsByUserIdAndPostId(userId, postId)
FavoriteRepository.existsByUserIdAndPostId(userId, postId)
    ↓
return { isLiked: true/false, isFavorited: true/false }
```

## 接口详情

### POST /api/post/{postId}/like 点赞

**响应**:
```json
{
  "code": 200,
  "message": "点赞成功",
  "data": null
}
```

### DELETE /api/post/{postId}/unlike 取消点赞

**响应**:
```json
{
  "code": 200,
  "message": "取消点赞成功",
  "data": null
}
```

### GET /api/post/{postId}/likeStatus 查询点赞状态

**响应**:
```json
{
  "code": 200,
  "data": {
    "isLiked": true
  }
}
```

### GET /api/user/{userId}/favorites 获取用户收藏列表

**响应**: 分页的PostResponse列表
