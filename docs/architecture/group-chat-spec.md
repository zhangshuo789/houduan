# 群聊功能设计文档

## 功能概述

群聊功能支持用户创建群组、添加/移除成员、发送消息等操作。

## 业务流程

### 1. 创建群聊

```
用户A创建群聊
    ↓
添加初始成员（可选）
    ↓
A自动成为群主（OWNER）
    ↓
返回群信息
```

### 2. 添加成员

```
群主/管理员 添加用户B
    ↓
直接加入，无需审核
    ↓
通知其他在线成员
```

### 3. 发送消息

```
用户发送消息
    ↓
检查是否被禁言
    ↓
保存消息，标记发送者已读
    ↓
SSE推送消息给所有在线成员
```

### 4. 退群与移除

```
- 普通成员可主动退群
- 群主不能退群（需先转让或解散）
- 只有群主可以移除成员
```

## 角色权限

| 操作 | 群主 (OWNER) | 管理员 (ADMIN) | 成员 (MEMBER) |
|------|--------------|-----------------|---------------|
| 转让群主 | ✓ | ✗ | ✗ |
| 设置管理员 | ✓ | ✗ | ✗ |
| 添加成员 | ✓ | ✓ | ✗ |
| 移除成员 | ✓ | ✓ (仅成员) | ✗ |
| 禁言/解禁 | ✓ | ✓ | ✗ |
| 发送消息 | ✓ | ✓ | ✓ (未禁言) |
| 退群 | ✗ | ✓ | ✓ |

## 接口列表

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/group` | POST | 创建群聊 | 登录 |
| `/api/group/my` | GET | 获取我的群聊列表 | 登录 |
| `/api/group/{id}` | GET | 获取群信息 | 公开 |
| `/api/group/{id}/members` | GET | 获取群成员列表 | 登录 |
| `/api/group/{id}/members` | POST | 添加群成员 | 群主/管理员 |
| `/api/group/{id}/members/{userId}` | DELETE | 移除群成员 | 群主 |
| `/api/group/{id}/members/{userId}/leave` | POST | 退群 | 成员 |
| `/api/group/{id}/ban/{userId}` | POST | 禁言 | 群主/管理员 |
| `/api/group/{id}/unban/{userId}` | DELETE | 解除禁言 | 群主/管理员 |
| `/api/group/{id}/messages` | GET | 获取群消息 | 成员 |
| `/api/group/{id}/messages` | POST | 发送群消息 | 成员(未禁言) |

## 数据模型

### 群聊存储

群聊使用 `message` 表存储，type = "group"，targetId = groupId

### GroupMember 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | BIGINT | 群聊ID (关联 message.id) |
| user_id | BIGINT | 用户ID |
| role | VARCHAR | OWNER/ADMIN/MEMBER |
| banned | BOOLEAN | 是否被禁言 |
| joined_at | DATETIME | 加入时间 |

## SSE 事件

群聊相关 SSE 事件：

| 事件名 | 触发时机 | data 内容 |
|--------|----------|-----------|
| newGroupMessage | 新群消息 | MessageResponse 对象 |

## 注意事项

1. **群主不能退群**：群主如需退出，必须先转让群主身份或解散群聊
2. **直接添加无需审核**：添加成员是直接加入，没有申请/审核流程
3. **禁言不影响其他权限**：被禁言的成员仍然可以查看消息、接收推送等
4. **消息已读标记**：发送消息时，发送者自动标记为已读
