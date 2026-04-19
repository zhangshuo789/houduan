# 14 管理后台模块

## 模块概述

管理后台模块提供系统管理、内容审核、用户管理等功能，仅对拥有ADMIN角色的用户开放。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   ├── AdminController.java              # 管理员接口
│   └── AdminSystemController.java        # 系统管理接口
├── service/
│   ├── AdminService.java                 # 管理业务逻辑
│   └── AdminLogAspect.java               # 管理员操作日志
├── repository/
│   ├── UserRepository.java               # 用户管理
│   ├── ReportRepository.java             # 举报数据访问
│   ├── AdminLogRepository.java           # 操作日志访问
│   ├── EventRepository.java              # 活动管理
│   └── ...
└── entity/
    ├── Report.java                      # 举报实体
    ├── AdminLog.java                     # 管理员操作日志
    ├── UserStatus.java                   # 用户状态
    └── SysRole.java                      # 系统角色
```

## 数据表结构

**report表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| reporter_id | BIGINT | 举报人ID |
| target_type | VARCHAR(20) | 举报类型: POST/COMMENT/USER |
| target_id | BIGINT | 被举报目标ID |
| reason | TEXT | 举报原因 |
| status | VARCHAR(20) | 状态: PENDING/HANDLED/REJECTED |
| handled_by | BIGINT | 处理人ID |
| created_at | DATETIME | 举报时间 |

**admin_log表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| admin_id | BIGINT | 管理员ID |
| action | VARCHAR(50) | 操作类型 |
| target_type | VARCHAR(20) | 目标类型 |
| target_id | BIGINT | 目标ID |
| detail | TEXT | 操作详情 |
| ip | VARCHAR(50) | IP地址 |
| created_at | DATETIME | 操作时间 |

**sys_user_role表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |

## 代码流转

### 管理员操作日志切面

```
AdminLogAspect 拦截所有 @AdminOperation 注解的方法
    ↓
提取方法参数中的 targetType, targetId
    ↓
提取当前管理员 ID (from SecurityContext)
    ↓
提取客户端 IP
    ↓
new AdminLog(adminId, action, targetType, targetId, detail, ip)
    ↓
AdminLogRepository.save(log)
```

### 处理举报

```
PUT /api/admin/report/{reportId}/handle
Authorization: Bearer <token> (ROLE_ADMIN)
    ↓
ReportRepository.findById(reportId)
    ↓
更新 status = HANDLED, handledBy = adminId
    ↓
根据举报类型执行相应操作:
  - POST: 删除帖子 / 忽略
  - COMMENT: 删除评论 / 忽略
  - USER: 禁用用户 / 警告
    ↓
return ApiResponse.success("处理完成")
```

### 禁用/启用用户

```
PUT /api/admin/user/{userId}/status
Authorization: Bearer <token> (ROLE_ADMIN)
    ↓
[DisableUserRequest] disabled, reason
    ↓
AdminService.disableUser(userId, disabled, reason)
    ↓
new UserStatus(userId, disabled, reason, now)
    ↓
UserStatusRepository.save(status)
    ↓
return ApiResponse.success(disabled ? "用户已禁用" : "用户已启用")
```

### 活动审核

```
管理员在 EventRegistrationController 中审核报名
    ↓
EventRegistrationService.approveRegistration()
    ↓
记录 AdminLog
```

## 接口保护

SecurityConfig 中配置：

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

无ADMIN角色的请求会被Spring Security拦截。

## 管理员操作日志

所有 `/api/admin/**` 接口的操作都会被记录：

```json
{
  "adminId": 1,
  "action": "HANDLE_REPORT",
  "targetType": "POST",
  "targetId": 123,
  "detail": "删除违规帖子",
  "ip": "192.168.1.1",
  "createdAt": "2026-04-19T10:00:00"
}
```
