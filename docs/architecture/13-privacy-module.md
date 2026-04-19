# 13 隐私设置模块

## 模块概述

隐私设置（Privacy）模块允许用户控制自己的个人信息、社交关系和消息接收的可见性。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── repository/
│   └── UserPrivacyRepository.java        # 隐私设置数据访问
├── service/
│   └── PrivacyService.java               # 隐私业务逻辑
└── entity/
    └── UserPrivacy.java                  # 隐私设置实体
```

## 数据表结构

**user_privacy表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID，唯一 |
| follow_list_visible | BOOLEAN | 关注列表是否公开 |
| follower_list_visible | BOOLEAN | 粉丝列表是否公开 |
| friends_only_receive | BOOLEAN | 是否仅接收好友消息 |

## 代码流转

### 获取或创建隐私设置

```
PrivacyService.getOrCreatePrivacySettings(userId)
    ↓
UserPrivacyRepository.findByUserId(userId)
    ↓ 存在 → return
    ↓ 不存在 → 创建默认设置（全公开，非好友可发消息）
    ↓ return
```

### 检查关注列表可见性

```
PrivacyService.isFollowListVisible(targetUserId)
    ↓
UserPrivacyRepository.findByUserId(targetUserId)
    ↓ followListVisible == true → 允许
    ↓ followListVisible == false
      当前用户 == targetUserId → 允许（查看自己的）
      否则 → 抛出ForbiddenException
```

### 检查粉丝列表可见性

```
PrivacyService.isFollowerListVisible(targetUserId)
    ↓ 同上逻辑
```

### 检查消息接收设置

```
PrivacyService.isFriendsOnlyReceive(targetUserId)
    ↓
UserPrivacyRepository.findByUserId(targetUserId)
    ↓ friendsOnlyReceive == false → 允许任何人
    ↓ friendsOnlyReceive == true
      存在双向关注 → 允许
      否则 → 抛出ForbiddenException("该用户只接收好友消息")
```

### 更新隐私设置

```
PUT /api/privacy
Authorization: Bearer <token>
    ↓
[PrivacySettingsRequest] followListVisible, followerListVisible, friendsOnlyReceive
    ↓
PrivacyService.updatePrivacySettings(userId, request)
    ↓
UserPrivacyRepository.findByUserId(userId)
    ↓ 更新字段
    ↓ UserPrivacyRepository.save(privacy)
    ↓ return ApiResponse.success("设置已更新")
```

## 默认隐私设置

新建用户的默认设置：
- `followListVisible = true` - 关注列表公开
- `followerListVisible = true` - 粉丝列表公开
- `friendsOnlyReceive = false` - 接收所有用户的消息

## 与其他模块的集成

### Follow模块

查看他人关注/粉丝列表时调用 `isFollowListVisible()` / `isFollowerListVisible()`

### Message模块

发送私信时调用 `isFriendsOnlyReceive()` 检查是否仅好友可收

## 接口详情

### GET /api/privacy 获取隐私设置

**响应**:
```json
{
  "code": 200,
  "data": {
    "followListVisible": true,
    "followerListVisible": true,
    "friendsOnlyReceive": false
  }
}
```

### PUT /api/privacy 更新隐私设置

**请求体**:
```json
{
  "followListVisible": false,
  "followerListVisible": false,
  "friendsOnlyReceive": true
}
```
