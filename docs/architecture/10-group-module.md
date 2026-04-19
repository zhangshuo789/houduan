# 10 群组模块

## 模块概述

群组（Group）模块实现多人聊天功能，群主可以创建群组、设置管理员、添加/移除成员，成员可以在群组中发送消息。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── GroupController.java              # 群组接口
├── service/
│   └── GroupService.java                 # 群组业务逻辑
├── repository/
│   ├── GroupRepository.java              # 群组数据访问
│   └── GroupMemberRepository.java         # 群成员数据访问
└── entity/
    ├── Group.java                       # 群组实体
    └── GroupMember.java                  # 群成员实体
```

## 数据表结构

**group_member表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | BIGINT | 群组ID |
| user_id | BIGINT | 用户ID |
| role | VARCHAR(20) | 角色: OWNER/ADMIN/MEMBER |
| banned | BOOLEAN | 是否被禁言 |
| joined_at | DATETIME | 加入时间 |

## 代码流转

### 创建群组

```
POST /api/group
Authorization: Bearer <token>
    ↓
[GroupRequest] name, description 参数
    ↓
GroupService.createGroup(ownerId, request)
    ↓
new Group(name, description, ownerId)
    ↓
GroupRepository.save(group)
    ↓
new GroupMember(groupId, ownerId, OWNER)
    ↓
GroupMemberRepository.save(member)
    ↓
return GroupResponse
```

### 获取群组信息

```
GET /api/group/{groupId}
    ↓
GroupService.getGroupInfo(groupId)
    ↓
GroupRepository.findById(groupId)
    ↓
GroupMemberRepository.countByGroupId 成员数
    ↓
return GroupResponse { id, name, description, memberCount, ... }
```

### 获取我的群聊列表

```
GET /api/group/my?page=0&size=10
Authorization: Bearer <token>
    ↓
GroupMemberRepository.findByUserId(userId)
    ↓
查询每个群的信息和成员数
    ↓
return Page<GroupResponse>
```

### 添加群成员

```
POST /api/group/{groupId}/members
Authorization: Bearer <token>
    ↓
GroupMemberRepository.findByGroupIdAndUserId(groupId, userId)
    ↓ 已是成员 → 抛出RuntimeException("已是群成员")
    ↓
GroupService.addMember(groupId, userId, actorId)
    ↓
检查操作者权限: OWNER 或 ADMIN
    ↓ 无权限 → 抛出RuntimeException("无权限")
    ↓
new GroupMember(groupId, userId, MEMBER)
    ↓
GroupMemberRepository.save(member)
    ↓
SseService通知群成员有新成员加入
    ↓
return ApiResponse.success("添加成功")
```

### 移除群成员

```
DELETE /api/group/{groupId}/members/{userId}
Authorization: Bearer <token>
    ↓
GroupService.removeMember(groupId, targetUserId, actorId)
    ↓
检查操作者权限:
  - OWNER 可以移除任何人
  - ADMIN 可以移除 MEMBER
  - 不能移除自己（OWNER转让权限后除外）
    ↓
GroupMemberRepository.deleteByGroupIdAndUserId
    ↓
return ApiResponse.success("移除成功")
```

### 禁言成员

```
POST /api/group/{groupId}/members/{userId}/ban
Authorization: Bearer <token>
    ↓
GroupService.banMember(groupId, targetUserId, actorId)
    ↓
检查操作者权限 (OWNER/ADMIN)
    ↓
GroupMemberRepository.updateBanned(groupId, userId, true)
    ↓
return ApiResponse.success("禁言成功")
```

### 在群组发送消息

```
POST /api/group/{groupId}/messages
Authorization: Bearer <token>
    ↓
GroupMemberRepository.findByGroupIdAndUserId(groupId, senderId)
    ↓ 不是成员 → 抛出RuntimeException("不是群成员")
    ↓
member.banned == true → 抛出RuntimeException("已被禁言")
    ↓
new Message(senderId, GROUP, groupId, content)
    ↓
MessageRepository.save(message)
    ↓
SseService广播消息给所有在线群成员
    ↓
return MessageResponse
```

### 获取群消息历史

```
GET /api/group/{groupId}/messages?page=0&size=50
Authorization: Bearer <token>
    ↓
GroupMemberRepository.existsByGroupIdAndUserId 检查是否是成员
    ↓
MessageRepository.findGroupMessages(groupId, pageable)
    ↓
return Page<MessageResponse>
```

## 角色权限

| 操作 | OWNER | ADMIN | MEMBER |
|------|-------|-------|--------|
| 转让群主 | ✓ | ✗ | ✗ |
| 设置管理员 | ✓ | ✗ | ✗ |
| 添加成员 | ✓ | ✓ | ✗ |
| 移除成员 | ✓ | ✓ (仅MEMBER) | ✗ |
| 禁言成员 | ✓ | ✓ | ✗ |
| 发送消息 | ✓ | ✓ | ✓ (未禁言) |

## 接口详情

### POST /api/group 创建群组

**请求体**:
```json
{
  "name": "string (1-50字符)",
  "description": "string (可选)"
}
```

### GET /api/group/my 获取我的群聊列表

**响应**:
```json
{
  "code": 200,
  "data": {
    "content": [
      { "id": 1, "name": "排球群", "description": "", "memberCount": 5, "createdAt": "..." }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

### GET /api/group/{groupId}/members 获取群成员列表

**响应**:
```json
{
  "code": 200,
  "data": [
    { "userId": 1, "nickname": "群主", "role": "OWNER", "joinedAt": "..." },
    { "userId": 2, "nickname": "管理员", "role": "ADMIN", "joinedAt": "..." }
  ]
}
```
