# 群聊功能设计文档

## 功能概述

群聊功能支持用户创建群组、添加/移除成员、发送消息等操作。

## 数据模型

### ChatGroup 表（群组信息）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 群名称 |
| description | VARCHAR(255) | 群描述 |
| avatar | VARCHAR(255) | 群头像（文件ID） |
| owner_id | BIGINT | 群主ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### GroupMember 表（群成员）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | BIGINT | 群ID（关联 chat_group.id） |
| user_id | BIGINT | 用户ID |
| role | VARCHAR(20) | OWNER/ADMIN/MEMBER |
| banned | BOOLEAN | 是否被禁言 |
| joined_at | DATETIME | 加入时间 |

**约束**：group_id + user_id 唯一

## 业务流程

### 1. 创建群聊

```
用户A创建群聊
    ↓
填写群名称（可选描述）
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
通知其他在线成员（可选）
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

### 5. 转让群主

```
群主选择新群主目标
    ↓
原群主变为 ADMIN
    ↓
新群主变为 OWNER
    ↓
更新群组的 owner_id
```

## 角色权限

| 操作 | 群主 (OWNER) | 管理员 (ADMIN) | 成员 (MEMBER) |
|------|--------------|-----------------|---------------|
| 转让群主 | ✓ | ✗ | ✗ |
| 设置/取消管理员 | ✓ | ✗ | ✗ |
| 修改群名称 | ✓ | ✗ | ✗ |
| 修改群描述 | ✓ | ✓ | ✗ |
| 修改群头像 | ✓ | ✓ | ✗ |
| 添加成员 | ✓ | ✓ | ✗ |
| 移除成员 | ✓ | ✗ | ✗ |
| 禁言/解禁 | ✓ | ✓ | ✗ |
| 发送消息 | ✓ | ✓ | ✓ (未禁言) |
| 退群 | ✗ | ✓ | ✓ |
| 解散群聊 | ✓ | ✗ | ✗ |

## 接口列表

> 基础路径：`/api/groups`

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/groups` | POST | 创建群聊 | 登录 |
| `/api/groups/my` | GET | 获取我的群聊列表 | 登录 |
| `/api/groups/{id}` | GET | 获取群信息 | 公开 |
| `/api/groups/{id}` | PUT | 修改群信息 | 群主/管理员 |
| `/api/groups/{id}/avatar` | PUT | 修改群头像 | 群主/管理员 |
| `/api/groups/{id}` | DELETE | 解散群聊 | 群主 |
| `/api/groups/{id}/members` | GET | 获取群成员列表 | 登录 |
| `/api/groups/{id}/members` | POST | 邀请成员 | 群主/管理员 |
| `/api/groups/{id}/members/{userId}` | DELETE | 移除成员 | 群主 |
| `/api/groups/{id}/members/{userId}/leave` | POST | 退群 | 成员 |
| `/api/groups/{id}/members/{userId}/ban` | POST | 禁言 | 群主/管理员 |
| `/api/groups/{id}/members/{userId}/unban` | DELETE | 解除禁言 | 群主/管理员 |
| `/api/groups/{id}/members/{userId}/admin` | POST | 设置/取消管理员 | 群主 |
| `/api/groups/{id}/transfer` | POST | 转让群主 | 群主 |
| `/api/groups/{id}/messages` | GET | 获取群消息 | 成员 |
| `/api/groups/{id}/messages` | POST | 发送群消息 | 成员(未禁言) |

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
5. **管理员权限限制**：管理员不能修改群名称，只能修改群描述和头像

## 数据库变更说明

### V5 群组表重构

- 新增 `chat_group` 表存储群组基本信息
- `group_member` 表重命名为 `chat_group_member`
- 群聊消息仍使用 `message` 表，type='group'，targetId 关联 chat_group.id
