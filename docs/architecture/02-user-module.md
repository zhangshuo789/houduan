# 02 用户模块

## 模块概述

负责用户信息管理、个人主页展示、用户统计数据查询等功能。

## 核心技术

| 技术 | 说明 |
|------|------|
| Spring Data JPA | 用户数据查询 |
| JPA EntityManager | 复杂统计查询 |
| Spring Security Context | 获取当前登录用户 |

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── UserController.java              # 用户接口
├── service/
│   └── UserService.java                 # 用户业务逻辑
├── repository/
│   └── UserRepository.java              # 用户数据访问
├── entity/
│   └── User.java                        # 用户实体
└── dto/request/
    └── UpdateUserRequest.java            # 更新用户请求
```

## 代码流转

### 获取用户信息

```
GET /api/user/{id}
    ↓
UserService.getUserById(id)
    ↓
UserRepository.findById(id)
    ↓ 未找到 → 抛出RuntimeException("用户不存在")
    ↓ 找到
    ↓
return User对象（密码字段已排除）
```

### 更新用户信息

```
PUT /api/user/{id}
    ↓
[UpdateUserRequest] 参数校验
    ↓
从SecurityContext获取当前登录用户
    ↓ 非本人且非管理员 → 抛出UnauthorizedException
    ↓
UserService.updateUser(id, request)
    ↓
UserRepository.findById(id)
    ↓
更新 nickname, avatar, bio 字段
    ↓
UserRepository.save(user)
    ↓
return ApiResponse.success("更新成功")
```

### 获取用户统计

```
GET /api/user/{id}/stats
    ↓
UserService.getUserStats(id)
    ↓
并行查询:
  - PostRepository.countByUserId(id)       → 帖子数
  - CommentRepository.countByUserId(id)   → 评论数
  - FollowRepository.countByFollowerId(id) → 粉丝数
  - FollowRepository.countByFolloweeId(id) → 关注数
  - LikeRepository.countByUserId(id)       → 获赞数
    ↓
组装StatsResponse { postCount, commentCount, followerCount, followingCount, likeCount }
    ↓
return ApiResponse.success(stats)
```

### 获取用户动态（Feed）

```
GET /api/user/{id}/feed?page=0&size=10
    ↓
UserService.getUserFeed(id, page, size)
    ↓
检查隐私设置: UserPrivacy.isFollowerListVisible()
    ↓ 当前用户未关注且设置了仅好友可见 → 抛出ForbiddenException
    ↓
PostRepository.findByUserIdOrderByCreatedAtDesc(id, pageable)
    ↓
PostService构建PostResponse列表（含点赞数、评论数、是否点赞、是否收藏）
    ↓
return ApiResponse.success(page)
```

## 接口详情

### GET /api/user/{id} 获取用户信息

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "排球达人",
    "avatar": "https://example.com/avatar.jpg",
    "bio": "热爱排球运动",
    "createdAt": "2026-01-01T10:00:00"
  }
}
```

### PUT /api/user/{id} 更新用户信息

**请求头**: `Authorization: Bearer <token>`

**请求体**:
```json
{
  "nickname": "新昵称",
  "avatar": "https://example.com/new-avatar.jpg",
  "bio": "新的个人简介"
}
```

### GET /api/user/{id}/stats 获取用户统计

**响应**:
```json
{
  "code": 200,
  "data": {
    "postCount": 42,
    "commentCount": 128,
    "followerCount": 256,
    "followingCount": 64,
    "likeCount": 1024
  }
}
```

### GET /api/user/{id}/posts 获取用户帖子列表

**查询参数**: `page`, `size`

**响应**: 分页的PostResponse列表
