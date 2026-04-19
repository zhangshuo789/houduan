# 排球社区后端技术架构文档

## 项目概述

**项目名称**: volleyball-community-backend
**技术栈**: Spring Boot 4.0.5 + Java 17 + MySQL + Spring Data JPA
**构建工具**: Maven
**安全框架**: Spring Security + JWT (jjwt 0.12.3)

---

## 目录结构

```
docs/architecture/
├── README.md                    # 本文档
├── 01-auth-module.md            # 认证模块
├── 02-user-module.md            # 用户模块
├── 03-board-module.md           # 板块模块
├── 04-post-module.md            # 帖子模块
├── 05-comment-module.md         # 评论模块
├── 06-like-favorite-module.md    # 点赞收藏模块
├── 07-file-module.md            # 文件上传模块
├── 08-follow-module.md          # 关注模块
├── 09-message-module.md         # 私信消息模块
├── 10-group-module.md           # 群组模块
├── 11-sse-module.md             # SSE实时通知模块
├── 12-event-module.md           # 活动模块
├── 13-privacy-module.md         # 隐私设置模块
├── 14-admin-module.md           # 管理后台模块
├── 15-sensitive-word-module.md  # 敏感词过滤模块
└── 16-security-architecture.md  # 安全架构设计
```

---

## 核心技术组件

### 1. JWT认证组件 (`JwtUtil.java`)

**位置**: `src/main/java/com/volleyball/volleyballcommunitybackend/util/JwtUtil.java`

**功能**: 生成和验证JWT令牌

**代码流转**:
```
用户登录 → AuthController.login()
    → AuthService.login() 验证用户名密码
    → JwtUtil.generateToken(userId, username) 生成Token
    → 返回 { token, userId, username }
```

**核心方法**:
- `generateToken(userId, username)` - 生成Token，包含userId和username声明
- `validateToken(token)` - 验证Token有效性
- `getUserIdFromToken(token)` - 从Token中提取userId
- `getUsernameFromToken(token)` - 从Token中提取username

### 2. 安全过滤链 (`SecurityConfig.java` + `JwtAuthenticationFilter`)

**位置**: `src/main/java/com/volleyball/volleyballcommunitybackend/config/SecurityConfig.java`

**请求流程**:
```
请求进入
    ↓
[JwtAuthenticationFilter] 提取Bearer Token
    ↓ Token有效 → 设置SecurityContextHolder.getContext().setAuthentication()
    ↓ Token无效/无Token → 继续过滤链（允许公开接口）
    ↓
[Controller层] 根据SecurityContext获取当前用户
```

**配置要点**:
- 公开接口: `/api/auth/**`, `/api/boards/**`, `/api/file/upload`, `GET /api/post/**`, `GET /api/user/**`, `GET /api/event/**`
- 受保护接口: 需要认证
- 管理员接口: `/api/admin/**` 需要 `ROLE_ADMIN`

### 3. 全局异常处理 (`GlobalExceptionHandler.java`)

**位置**: `src/main/java/com/volleyball/volleyballcommunitybackend/exception/GlobalExceptionHandler.java`

**异常处理映射**:
| 异常类型 | HTTP状态码 | 返回格式 |
|---------|-----------|---------|
| MethodArgumentNotValidException | 400 | { code: 400, message: "验证失败", errors: [...] } |
| RuntimeException | 400 | { code: 400, message: exception.getMessage() } |
| Exception | 500 | { code: 500, message: "服务器内部错误" } |

### 4. 请求日志切面 (`RequestLogAspect.java`)

**位置**: `src/main/java/com/volleyball/volleyballcommunitybackend/common/log/RequestLogAspect.java`

**切点**: `execution(* com.volleyball.volleyballcommunitybackend..controller..*.*(..))`

**敏感数据过滤**: 手机号、身份证、用户名、密码、地址、生日等字段自动脱敏

---

## 模块索引

| 模块 | 描述 | 核心Entity |
|------|------|-----------|
| [01-认证模块](01-auth-module.md) | 用户注册、登录、JWT发放 | User |
| [02-用户模块](02-user-module.md) | 用户信息管理、个人主页、用户统计 | User |
| [03-板块模块](03-board-module.md) | 帖子板块分类管理 | Board |
| [04-帖子模块](04-post-module.md) | 帖子CRUD、敏感词过滤 | Post |
| [05-评论模块](05-comment-module.md) | 帖子评论、嵌套回复 | Comment |
| [06-点赞收藏模块](06-like-favorite-module.md) | 帖子点赞和收藏 | Like, Favorite |
| [07-文件上传模块](07-file-module.md) | 文件上传、存储、访问 | FileEntity |
| [08-关注模块](08-follow-module.md) | 用户关注、粉丝列表 | Follow |
| [09-私信消息模块](09-message-module.md) | 私聊消息、会话列表 | Message |
| [10-群组模块](10-group-module.md) | 群聊管理、群成员 | GroupMember, Message |
| [11-SSE实时通知](11-sse-module.md) | 服务端推送实时通知 | - |
| [12-活动模块](12-event-module.md) | 活动发布、报名管理 | Event, EventRegistration |
| [13-隐私设置模块](13-privacy-module.md) | 用户隐私偏好 | UserPrivacy |
| [14-管理后台模块](14-admin-module.md) | 系统管理、内容审核 | Report, AdminLog |
| [15-敏感词过滤模块](15-sensitive-word-module.md) | 内容安全过滤 | SensitiveWord |
| [16-安全架构设计](16-security-architecture.md) | 安全机制详解 | - |
