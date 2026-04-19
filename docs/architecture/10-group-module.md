# 10 群组模块

## 模块概述

群组（Group）模块实现多人聊天功能，群主可以创建群组、设置管理员、添加/移除成员，成员可以在群组中发送消息。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── GroupController.java              # 群组接口
├── service/
│   ├── GroupService.java                 # 群组业务逻辑
│   └── AdminGroupService.java           # 管理员群组业务逻辑
├── repository/
│   ├── ChatGroupRepository.java          # 群组数据访问
│   └── GroupMemberRepository.java       # 群成员数据访问
└── entity/
    ├── ChatGroup.java                   # 群组实体
    └── GroupMember.java                 # 群成员实体
```

## 数据表结构

### chat_group 表（群组信息）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 群名称 |
| description | VARCHAR(255) | 群描述 |
| avatar | VARCHAR(255) | 群头像（文件ID） |
| owner_id | BIGINT | 群主ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### chat_group_member 表（群成员）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | BIGINT | 群组ID（关联 chat_group.id） |
| user_id | BIGINT | 用户ID |
| role | VARCHAR(20) | 角色: OWNER/ADMIN/MEMBER |
| banned | BOOLEAN | 是否被禁言 |
| joined_at | DATETIME | 加入时间 |

**约束**：group_id + user_id 唯一

## 代码流转

### 创建群组

```
POST /api/groups
Authorization: Bearer <token>
    ↓
[GroupRequest] name, description, memberIds 参数
    ↓
GroupService.createGroup(ownerId, request)
    ↓
new ChatGroup(name, description, ownerId)
    ↓
ChatGroupRepository.save(group)
    ↓
new GroupMember(groupId, ownerId, OWNER)
    ↓
GroupMemberRepository.save(member)
    ↓ (可选) 添加其他成员
return GroupResponse
```

### 获取群组信息

```
GET /api/groups/{groupId}
    ↓
GroupService.getGroupInfo(groupId)
    ↓
ChatGroupRepository.findById(groupId)
    ↓
GroupMemberRepository.countByGroupId 成员数
    ↓
return GroupResponse { id, name, description, avatar, memberCount, ... }
```

### 获取我的群聊列表

```
GET /api/groups/my?page=0&size=10
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
POST /api/groups/{groupId}/members?userId={userId}
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
return ApiResponse.success("添加成功")
```

### 移除群成员

```
DELETE /api/groups/{groupId}/members/{userId}
Authorization: Bearer <token>
    ↓
GroupService.removeMember(groupId, targetUserId, actorId)
    ↓
检查操作者权限: 仅 OWNER
    ↓ 无权限 → 抛出RuntimeException("无权限")
    ↓
GroupMemberRepository.deleteByGroupIdAndUserId
    ↓
return ApiResponse.success("移除成功")
```

### 禁言成员

```
POST /api/groups/{groupId}/members/{userId}/ban
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

### 设置/取消管理员

```
POST /api/groups/{groupId}/members/{userId}/admin?setAdmin=true
Authorization: Bearer <token>
    ↓
GroupService.setAdmin(groupId, targetUserId, actorId, setAdmin)
    ↓
检查操作者权限: 仅 OWNER
    ↓
GroupMemberRepository.updateRole(groupId, userId, ADMIN/MEMBER)
    ↓
return ApiResponse.success("设置管理员成功")
```

### 转让群主

```
POST /api/groups/{groupId}/transfer?newOwnerId={newOwnerId}
Authorization: Bearer <token>
    ↓
GroupService.transferOwner(groupId, newOwnerId, actorId)
    ↓
检查操作者权限: 仅 OWNER
    ↓
更新原群主为 MEMBER
    ↓
更新新群主为 OWNER
    ↓
更新 ChatGroup.ownerId
    ↓
return ApiResponse.success("转让成功")
```

### 在群组发送消息

```
POST /api/groups/{groupId}/messages
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
GET /api/groups/{groupId}/messages?page=0&size=50
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
| 设置/取消管理员 | ✓ | ✗ | ✗ |
| 修改群名称 | ✓ | ✗ | ✗ |
| 修改群描述/头像 | ✓ | ✓ | ✗ |
| 添加成员 | ✓ | ✓ | ✗ |
| 移除成员 | ✓ | ✗ | ✗ |
| 禁言成员 | ✓ | ✓ | ✗ |
| 发送消息 | ✓ | ✓ | ✓ (未禁言) |
| 退群 | ✗ | ✓ | ✓ |
| 解散群聊 | ✓ | ✗ | ✗ |

## 接口详情

### POST /api/groups 创建群组

**请求体**:
```json
{
  "name": "string (1-50字符，必填)",
  "description": "string (可选)",
  "memberIds": [1, 2, 3] // 可选，初始成员ID列表
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "排球群",
    "description": "排球爱好者群",
    "avatar": null,
    "type": "group",
    "memberCount": 4,
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

### GET /api/groups/my 获取我的群聊列表

**响应**:
```json
{
  "code": 200,
  "data": {
    "content": [
      { "id": 1, "name": "排球群", "description": "", "avatar": null, "type": "group", "memberCount": 5, "createdAt": "..." }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

### GET /api/groups/{groupId}/members 获取群成员列表

**响应**:
```json
{
  "code": 200,
  "data": [
    { "userId": 1, "nickname": "群主", "avatar": "http://...", "role": "OWNER", "banned": false, "joinedAt": "..." },
    { "userId": 2, "nickname": "管理员", "avatar": "http://...", "role": "ADMIN", "banned": false, "joinedAt": "..." }
  ]
}
```

### PUT /api/groups/{groupId} 修改群信息

**请求体**:
```json
{
  "name": "string (可选)",
  "description": "string (可选)"
}
```

**注意**：群主可修改所有字段，管理员只能修改 description

### POST /api/groups/{groupId}/transfer 转让群主

**注意**：仅群主可操作，转让后原群主变为普通成员
