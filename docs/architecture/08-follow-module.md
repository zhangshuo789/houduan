# 08 关注模块

## 模块概述

关注（Follow）模块实现用户之间的关注关系，支持关注、取消关注、获取关注列表、粉丝列表和好友列表（双向关注）功能。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── FollowController.java            # 关注接口
├── service/
│   └── FollowService.java                # 关注业务逻辑
├── repository/
│   └── FollowRepository.java             # 关注数据访问
└── entity/
    └── Follow.java                      # 关注实体
```

## 数据表结构

**follow表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| follower_id | BIGINT | 关注者ID |
| followee_id | BIGINT | 被关注者ID |
| created_at | DATETIME | 关注时间 |
| **UNIQUE** | | (follower_id, followee_id) 防止重复关注 |

## 代码流转

### 关注用户

```
POST /api/follow/{userId}
Authorization: Bearer <token>
    ↓
FollowService.followUser(followerId, followeeId)
    ↓
followerId == followeeId → 抛出RuntimeException("不能关注自己")
    ↓
FollowRepository.existsByFollowerIdAndFolloweeId 防止重复关注
    ↓
new Follow(followerId, followeeId)
    ↓
FollowRepository.save(follow)
    ↓
return ApiResponse.success("关注成功")
```

### 取消关注

```
DELETE /api/follow/{userId}
Authorization: Bearer <token>
    ↓
FollowService.unfollowUser(followerId, followeeId)
    ↓
FollowRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId)
    ↓
return ApiResponse.success("取消关注成功")
```

### 获取关注列表

```
GET /api/user/{userId}/following?page=0&size=20
    ↓
FollowService.getFollowingList(userId, pageable)
    ↓
FollowRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable)
    ↓
UserRepository.findAllById(followeeIds) 批量获取用户信息
    ↓
返回分页的User列表
```

### 获取粉丝列表

```
GET /api/user/{userId}/followers?page=0&size=20
    ↓
FollowService.getFollowerList(userId, pageable)
    ↓
FollowRepository.findByFolloweeIdOrderByCreatedAtDesc(userId, pageable)
    ↓
UserRepository.findAllById(followerIds)
    ↓
返回分页的User列表
```

### 获取好友列表（双向关注）

```
GET /api/user/{userId}/friends?page=0&size=20
Authorization: Bearer <token>
    ↓
FollowService.getFriendsList(userId, pageable)
    ↓
1. 获取userId关注的用户列表 following
2. 获取关注userId的用户列表 followers
3. 取交集 = 双向关注的用户
    ↓
返回分页的好友User列表
```

### 查询关注状态

```
GET /api/follow/{userId}/status
Authorization: Bearer <token>
    ↓
FollowService.getFollowStatus(currentUserId, targetUserId)
    ↓
FollowRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetUserId)
FollowRepository.existsByFollowerIdAndFolloweeId(targetUserId, currentUserId)
    ↓
return {
  "isFollowing": true/false,   // 当前用户是否关注了目标
  "isFollowed": true/false     // 目标用户是否关注了当前用户
}
```

## 隐私控制

用户可以通过隐私设置（参见 [13-隐私设置模块](13-privacy-module.md)）控制：
- `followListVisible` - 是否公开自己的关注列表
- `followerListVisible` - 是否公开自己的粉丝列表

查询他人关注/粉丝列表时，会检查隐私设置：

```
GET /api/user/{userId}/following
    ↓
PrivacyService.isFollowListVisible(targetUserId)
    ↓ 未公开 → 抛出RuntimeException("该用户未公开关注列表")
    ↓
返回关注列表
```

## 接口详情

### POST /api/follow/{userId} 关注用户

**响应**:
```json
{
  "code": 200,
  "message": "关注成功",
  "data": null
}
```

### GET /api/follow/{userId}/status 查询关注状态

**响应**:
```json
{
  "code": 200,
  "data": {
    "isFollowing": true,
    "isFollowed": false
  }
}
```

### GET /api/user/{userId}/friends 获取好友列表

**响应**: 分页的用户列表
