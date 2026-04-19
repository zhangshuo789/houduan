# 项目文档

> 本文档维护项目业务流程、技术细节和架构设计

---

## 项目概述

- **项目名称**：volleyball-community-backend
- **定位**：排球社区后端服务，打造排球版的"知乎"或"贴吧"
- **技术栈**：Spring Boot 4.0.5 + Java 17 + MySQL + Maven
- **包名**：`com.volleyball.volleyballcommunitybackend`

---

## 数据库配置

- **地址**：`localhost:3306`
- **数据库名**：`volleyball_community`
- **用户名**：`root`
- **密码**：`123456`

---

## 数据库设计

### 阶段一 ER 图（已完成）

```
┌─────────┐       ┌─────────┐       ┌─────────┐
│  User   │       │  Post   │       │  Board  │
├─────────┤       ├─────────┤       ├─────────┤
│ id (PK) │───┐   │ id (PK) │       │ id (PK) │
│username │   └───│ user_id │       │  name   │
│password │       │board_id │───┐   │  icon   │
│ nickname│       │  title  │   └───│   -     │
│  avatar │       │ content │       └─────────┘
│   bio   │       │createdAt│
│createdAt│       │updatedAt│
└─────────┘       └─────────┘
```

### 字段说明

#### User 用户表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 登录用户名，唯一 |
| password | VARCHAR(255) | BCrypt加密存储 |
| nickname | VARCHAR(50) | 显示名称 |
| avatar | VARCHAR(255) | 头像URL地址 |
| bio | VARCHAR(255) | 个人简介 |
| created_at | DATETIME | 创建时间 |

#### Board 板块表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 板块名称 |
| description | VARCHAR(255) | 板块描述 |
| icon | VARCHAR(50) | 板块图标 |
| created_at | DATETIME | 创建时间 |

**初始数据**：技术讨论🏐、赛事资讯🏆、装备评测👟、约球专区🤝

#### Post 帖子表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(100) | 标题 |
| content | TEXT | 内容，支持Markdown |
| user_id | BIGINT | 发帖用户ID |
| board_id | BIGINT | 所属板块ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### Comment 评论表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| content | TEXT | 评论内容 |
| user_id | BIGINT | 评论用户ID |
| post_id | BIGINT | 被评论的帖子ID |
| parent_id | BIGINT | 父评论ID（为空表示一级评论） |
| created_at | DATETIME | 创建时间 |

#### Like 点赞表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 点赞用户ID |
| post_id | BIGINT | 被点赞的帖子ID |
| created_at | DATETIME | 创建时间 |

**约束**：user_id + post_id 唯一

#### Favorite 收藏表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 收藏用户ID |
| post_id | BIGINT | 被收藏的帖子ID |
| created_at | DATETIME | 创建时间 |

**约束**：user_id + post_id 唯一

#### File 文件表（已完成）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| file_name | VARCHAR(255) | 原始文件名 |
| stored_name | VARCHAR(255) | 存储文件名（UUID） |
| file_path | VARCHAR(500) | 存储路径（相对路径） |
| file_type | VARCHAR(50) | 文件类型（avatar/post_image） |
| content_type | VARCHAR(100) | MIME类型 |
| file_size | BIGINT | 文件大小（字节） |
| user_id | BIGINT | 上传用户ID |
| created_at | DATETIME | 创建时间 |

#### Follow 关注表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| follower_id | BIGINT | 关注者ID |
| followee_id | BIGINT | 被关注者ID |
| created_at | DATETIME | 创建时间 |

**约束**：follower_id + followee_id 唯一

#### Friendship 好友表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| friend_id | BIGINT | 好友ID |
| created_at | DATETIME | 创建时间 |

**约束**：user_id + friend_id 唯一

#### UserPrivacy 隐私设置表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID（唯一） |
| follow_list_visible | BOOLEAN | 关注列表是否可见 |
| follower_list_visible | BOOLEAN | 粉丝列表是否可见 |
| friends_only_receive | BOOLEAN | 是否仅接收好友消息 |

#### Message 消息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| sender_id | BIGINT | 发送者ID |
| type | VARCHAR(20) | 消息类型（private/group） |
| target_id | BIGINT | 接收者ID或群ID |
| content | TEXT | 消息内容 |
| created_at | DATETIME | 创建时间 |

#### MessageRead 消息已读表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| message_id | BIGINT | 消息ID |
| user_id | BIGINT | 用户ID |
| read_at | DATETIME | 已读时间（为空表示未读） |

**约束**：message_id + user_id 唯一

#### AdminNotification 管理员通知表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| type | VARCHAR(20) | 通知类型（BROADCAST/PRIVATE） |
| title | VARCHAR(100) | 通知标题 |
| content | TEXT | 通知内容 |
| target_user_id | BIGINT | 目标用户ID（null表示全体） |
| is_read | BOOLEAN | 是否已读 |
| sent_at | DATETIME | 发送时间 |

#### GroupMember 群成员表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| group_id | BIGINT | 群ID |
| user_id | BIGINT | 用户ID |
| role | VARCHAR(20) | 角色（OWNER/ADMIN/MEMBER） |
| banned | BOOLEAN | 是否被禁言 |
| joined_at | DATETIME | 加入时间 |

**约束**：group_id + user_id 唯一

---

## 安全机制

### JWT Token

- **算法**：HS256
- **有效期**：30天
- **存储位置**：用户登录成功后返回，前端存储在 localStorage
- **传递方式**：`Authorization: Bearer <token>`

### 接口权限

| 接口 | 权限要求 |
|------|----------|
| POST /api/auth/register | 公开 |
| POST /api/auth/login | 公开 |
| GET /api/user/{id} | 公开 |
| PUT /api/user/{id} | 登录（本人） |
| GET /api/boards | 公开 |
| GET /api/boards/{id}/posts | 公开 |
| POST /api/post | 登录 |
| GET /api/post/{id} | 公开 |
| PUT /api/post/{id} | 登录（本人） |
| DELETE /api/post/{id} | 登录（本人） |
| POST /api/post/{id}/comment | 登录 |
| GET /api/post/{id}/comments | 公开 |
| DELETE /api/comment/{id} | 登录（本人） |
| POST /api/post/{id}/like | 登录 |
| DELETE /api/post/{id}/unlike | 登录 |
| GET /api/post/{id}/likeStatus | 公开 |
| POST /api/post/{id}/favorite | 登录 |
| DELETE /api/post/{id}/unfavorite | 登录 |
| GET /api/post/{id}/favoriteStatus | 公开 |
| GET /api/user/{id}/favorites | 公开 |
| POST /api/file/upload | 登录 |
| GET /api/file/{id} | 登录 |
| GET /api/file/{id}/url | 登录 |

---

## 业务流程

### 用户注册流程

```
1. 前端提交 username, password, nickname
2. 后端校验：
   - username 4-20字符，唯一
   - password 6-20字符
   - nickname 2-20字符
3. 密码 BCrypt 加密
4. 保存用户
5. 返回用户信息（不含token）
```

### 用户登录流程

```
1. 前端提交 username, password
2. 后端校验用户名密码
3. 生成 JWT Token（userId + username + 30天有效期）
4. 返回 token + 用户信息
```

### 发帖流程

```
1. 前端带 token 请求 POST /api/post
2. 后端验证 token，确认用户身份
3. 校验 title, content, boardId
4. 创建帖子记录
5. 返回帖子信息（含发布者信息）
```

---

## 项目结构

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── VolleyballCommunityBackendApplication.java    # 启动类
├── config/
│   ├── SecurityConfig.java                     # Spring Security配置 + JWT过滤器
│   └── FileProperties.java                     # 文件服务配置
├── controller/
│   ├── AuthController.java                     # 认证接口（注册/登录）
│   ├── UserController.java                    # 用户接口（查询/更新）
│   ├── BoardController.java                   # 板块接口（列表/帖子列表）
│   ├── PostController.java                    # 帖子接口（CRUD）
│   ├── CommentController.java                 # 评论接口
│   ├── LikeController.java                    # 点赞接口
│   ├── FavoriteController.java                # 收藏接口
│   └── FileController.java                    # 文件上传接口
├── entity/
│   ├── User.java                              # 用户实体
│   ├── Board.java                             # 板块实体
│   ├── Post.java                              # 帖子实体
│   ├── Comment.java                           # 评论实体
│   ├── Like.java                              # 点赞实体
│   ├── Favorite.java                           # 收藏实体
│   └── FileEntity.java                         # 文件实体
├── repository/
│   ├── UserRepository.java                    # 用户数据访问
│   ├── BoardRepository.java                    # 板块数据访问
│   ├── PostRepository.java                     # 帖子数据访问
│   ├── CommentRepository.java                  # 评论数据访问
│   ├── LikeRepository.java                     # 点赞数据访问
│   ├── FavoriteRepository.java                 # 收藏数据访问
│   └── FileRepository.java                     # 文件数据访问
├── service/
│   ├── AuthService.java                        # 认证业务
│   ├── UserService.java                        # 用户业务
│   ├── BoardService.java                       # 板块业务
│   ├── PostService.java                        # 帖子业务
│   ├── CommentService.java                     # 评论业务
│   ├── LikeService.java                       # 点赞业务
│   ├── FavoriteService.java                   # 收藏业务
│   └── FileService.java                       # 文件上传业务
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── PostRequest.java
│   │   ├── UpdateUserRequest.java
│   │   └── CommentRequest.java
│   └── response/
│       ├── ApiResponse.java                    # 统一响应封装
│       ├── LoginResponse.java                  # 登录响应（含token）
│       ├── PostResponse.java                   # 帖子响应
│       ├── PostDetailResponse.java             # 帖子详情响应（含点赞/收藏/评论数）
│       ├── CommentResponse.java                # 评论响应
│       └── FileResponse.java                   # 文件响应
├── common/
│   └── log/
│       ├── LogUtils.java                       # 日志工具类（敏感信息过滤）
│       ├── RequestLogAspect.java               # AOP请求日志切面
│       └── SensitiveDataFilter.java            # 敏感信息过滤器
├── util/
│   └── JwtUtil.java                            # JWT工具类
└── exception/
    └── GlobalExceptionHandler.java             # 全局异常处理
```

---

## 日志设计

### 概述

项目集成完整的日志系统，包含敏感信息自动过滤、请求/响应日志记录、多环境配置。

### 核心组件

| 文件 | 功能 |
|------|------|
| `LogUtils.java` | 日志工具类，集成敏感信息过滤，支持 MDC 链路追踪 |
| `RequestLogAspect.java` | AOP 切面，自动记录 Controller 请求/响应、耗时 |
| `SensitiveDataFilter.java` | 敏感信息过滤器，正则匹配手机号/身份证/银行卡/邮箱等 |
| `logback-spring.xml` | 多环境日志配置（dev/test/pro），滚动策略 |

### 敏感信息过滤

自动过滤以下敏感数据：
- **密码类**：password, pwd, passwd, token, secret
- **证件类**：身份证号、手机号、银行卡号
- **联系类**：邮箱、电话
- **其他**：地址、真实姓名等

### 日志级别

| 环境 | 级别 | 输出 |
|------|------|------|
| dev | DEBUG | 控制台彩色日志 |
| test | INFO | 控制台 + 文件（100MB滚动） |
| pro | INFO | 分离文件：request.log / app.log / error.log |

### 使用示例

```java
// 1. 获取Logger
Logger logger = LogUtils.getLogger();

// 2. 普通日志
LogUtils.info(logger, "用户登录: {}", username);

// 3. 敏感字段自动过滤（字段名匹配）
LogUtils.info(logger, "登录信息: {}", loginRequest);

// 4. MDC链路追踪
LogUtils.setMdc("userId", userId.toString());
LogUtils.info(logger, "操作完成");
LogUtils.clearMdc();
```

### 请求日志示例

```
2026-04-12 10:00:00.000 [http-nio-8080-exec-1] INFO  c.v.v.common.log - >>> REQUEST: POST /api/auth/login | {"username":"user1","password":"***"}
2026-04-12 10:00:00.100 [http-nio-8080-exec-1] DEBUG c.v.v.common.log - <<< RESPONSE: POST /api/auth/login | 100ms | result: {"token":"***JWT_TOKEN***","user":{...}}
```

---

## 开发进度

### 阶段一：项目骨架 & 用户系统 ✅ 已完成
- [x] 用户注册/登录
- [x] 用户信息查询/更新
- [x] 板块列表
- [x] 帖子CRUD
- [x] 日志系统（敏感信息过滤、AOP请求日志）

### 阶段二：互动功能 ✅ 已完成
- [x] 评论（comment）- 支持二级评论、分页
- [x] 点赞（like）- 点赞/取消点赞/状态查询
- [x] 收藏（favorite）- 收藏/取消收藏/状态查询/列表
- [x] 帖子详情返回点赞数/收藏数/评论数

### 文件服务 ✅ 已完成
- [x] 文件上传（avatar/post_image）
- [x] 文件访问（返回完整URL）
- [x] 本地存储策略

### 阶段三：社交功能 ✅ 已完成
- [x] 关注/粉丝（follow）
- [x] 好友关系（friendship）
- [x] 隐私设置（privacy）
- [x] 私信（message）
- [x] 群聊（group）
- [x] SSE实时推送

### 阶段四：赛事日历 ✅ 已完成
- [x] 赛事发布（event）
- [x] 赛事订阅（event_subscription）
- [x] 赛事报名（event_registration）
- [x] 报名审核（registration review）
- [x] SSE实时通知（event updates, registration results）

### 阶段五：管理员端 ✅ 已完成
### 阶段六：知识图谱 🔄 规划中

---

## 枚举字段设计

### 用户角色 (User Role)
| 值 | 说明 |
|----|------|
| USER | 普通用户（默认） |
| ADMIN | 管理员 |

### 赛事类型 (Event Type)
| 值 | 说明 |
|----|------|
| MATCH | 正式比赛，需管理员发布 |
| ACTIVITY | 活动（如约球、训练营），登录用户可发布 |

### 赛事状态 (Event Status)
| 值 | 说明 |
|----|------|
| PREPARING | 筹备中 |
| REGISTERING | 报名中 |
| IN_PROGRESS |已开始 |
| ENDED | 已结束 |
| CANCELLED | 已取消 |

### 报名状态 (Registration Status)
| 值 | 说明 |
|----|------|
| PENDING | 待审核 |
| APPROVED | 已通过 |
| REJECTED | 已拒绝 |

### 消息类型 (Message Type)
| 值 | 说明 |
|----|------|
| private | 私信 |
| group | 群消息 |

### 群成员角色 (Group Member Role)
| 值 | 说明 |
|----|------|
| OWNER | 群主 |
| ADMIN | 管理员 |
| MEMBER | 普通成员 |

---

## 业务流程总览

### 用户端核心流程

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户端流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [注册] → [登录] → [浏览帖子/赛事] → [互动(点赞/评论/收藏)]        │
│                           ↓                                     │
│                    [发布内容(帖子/活动)]                         │
│                           ↓                                     │
│                    [社交(关注/私信/群聊)]                        │
│                           ↓                                     │
│              [订阅赛事/报名赛事/接收通知]                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 内容发布流程

```
1. 发帖流程：
   用户登录 → 选择板块 → 编写标题内容 → 上传图片(可选) → 发布帖子

2. 活动发布流程：
   用户登录 → 创建活动 → 填写信息(时间/地点/费用) → 发布活动 → 接收订阅/报名

3. 赛事发布流程：
   管理员登录 → 创建比赛 → 填写信息 → 发布比赛 → 用户订阅/报名
```

### 社交互动流程

```
关注流程：用户A关注用户B → A成为B的粉丝 → B收到通知
好友流程：用户A添加B为好友 → B同意 → 双方成为好友 → 可私信
群聊流程：创建群 → 邀请成员 → 群内聊天 → 群主/管理员管理成员
```

### 赛事参与流程

```
订阅：浏览赛事列表 → 订阅赛事 → 接收赛事更新通知
报名：浏览赛事 → 填写报名信息 → 等待审核 → 接收审核结果通知
审核：主办方查看报名列表 → 审核通过/拒绝 → 报名者收到SSE通知
状态通知：定时任务监控赛事时间 → 自动变更状态 → SSE通知订阅者
```

### 赛事状态自动变更机制

系统通过定时任务（每分钟执行）自动监控赛事状态：

| 状态 | 说明 | 何时变为下一状态 |
|------|------|-----------------|
| REGISTERING | 报名中 | 新建赛事默认状态；start_time 到期时变为 IN_PROGRESS |
| IN_PROGRESS | 进行中 | start_time 到期时自动变为进行中 |
| ENDED | 已结束 | end_time 到期时自动变为已结束 |
| CANCELLED | 已取消 | 主办方或管理员手动取消 |

状态变更时，通过 SSE 事件通知所有订阅者。

### 管理员端流程

```
┌─────────────────────────────────────────────────────────────────┐
│                       管理员端流程                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [登录] → [仪表盘概览] → [用户/内容/赛事/群聊/配置]              │
│                           ↓                                     │
│  用户管理：用户列表 → 搜索筛选 → 设置角色/禁用用户                 │
│                           ↓                                     │
│  内容审核：查看举报 → 审核内容 → 处理结果(删除/警告/驳回)          │
│                           ↓                                     │
│  敏感词配置：添加/编辑敏感词 → 设置替换词 → 实时生效              │
│                           ↓                                     │
│  数据统计：查看运营/用户/内容统计 → 数据概览                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 阶段五：管理员端功能实现 ✅ 已完成

### 管理员后台模块

| 功能 | 说明 | 状态 |
|------|------|------|
| 用户管理 | 查看用户列表、禁用/启用用户、设置管理员 | ✅ |
| 内容审核 | 帖子举报处理、评论删除、违规内容处置 | ✅ |
| 赛事管理 | 赛事列表、赛事审核( MATCH类型)、报名管理 | ✅ |
| 群聊管理 | 群列表、群主变更、违规群封禁 | ✅ |
| 数据统计 | 日活/月活、发帖量、互动量、赛事参与统计 | ✅ |
| 系统配置 | 板块管理、敏感词过滤、通知模板 | ✅ |

### 内置管理员账号
- 用户名：`admin`
- 密码：`123456`

### 数据模型扩展

#### 用户角色表 (sys_role)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(20) | 角色名(ADMIN) |
| description | VARCHAR(100) | 描述 |
| created_at | DATETIME | 创建时间 |

#### 用户角色关联表 (sys_user_role)
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |

#### 内容举报表 (report)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| reporter_id | BIGINT | 举报人ID |
| target_type | VARCHAR(20) | 举报类型(post/comment/event) |
| target_id | BIGINT | 被举报内容ID |
| reason | TEXT | 举报原因 |
| status | VARCHAR(20) | 状态(PENDING/HANDLED/REJECTED) |
| handled_by | BIGINT | 处理人ID |
| handled_at | DATETIME | 处理时间 |
| created_at | DATETIME | 举报时间 |

#### 操作日志表 (admin_log)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| admin_id | BIGINT | 管理员ID |
| action | VARCHAR(50) | 操作类型 |
| target_type | VARCHAR(20) | 操作对象类型 |
| target_id | BIGINT | 操作对象ID |
| detail | TEXT | 操作详情 |
| ip | VARCHAR(50) | IP地址 |
| created_at | DATETIME | 操作时间 |

### API 接口规划

```
# 管理员接口 /api/admin/* (需ADMIN角色)

## 用户管理
GET    /api/admin/users                    # 用户列表(分页、搜索、状态筛选)
PUT    /api/admin/users/{id}/role         # 设置用户角色
PUT    /api/admin/users/{id}/status       # 禁用/启用用户

## 内容审核
POST   /api/admin/reports                  # 用户提交举报
GET    /api/admin/reports                  # 举报列表(状态筛选)
PUT    /api/admin/reports/{id}            # 处理举报
GET    /api/admin/reports/pending         # 待处理举报数量

## 内容管理
DELETE /api/admin/posts/{id}              # 删除违规帖子
DELETE /api/admin/comments/{id}           # 删除违规评论

## 赛事管理
GET    /api/admin/events                  # 赛事列表(所有类型)
PUT    /api/admin/events/{id}/status      # 修改赛事状态
GET    /api/admin/events/{id}/registrations # 赛事报名列表

## 群聊功能
POST   /api/group                          # 创建群聊
GET    /api/group/my                      # 获取我的群聊列表
GET    /api/group/{id}                   # 获取群信息
GET    /api/group/{id}/members            # 获取群成员列表
POST   /api/group/{id}/members            # 添加群成员
DELETE /api/group/{id}/members/{userId}   # 移除群成员
POST   /api/group/{id}/members/{userId}/leave # 退群
POST   /api/group/{id}/ban/{userId}      # 禁言成员
DELETE /api/group/{id}/unban/{userId}     # 解除禁言
GET    /api/group/{id}/messages          # 获取群消息列表
POST   /api/group/{id}/messages          # 发送群消息

## 管理员群聊管理
GET    /api/admin/groups                   # 群列表
PUT    /api/admin/groups/{id}/owner       # 更换群主
DELETE /api/admin/groups/{id}             # 解散违规群

## 数据统计
GET    /api/admin/stats/overview           # 运营概览
GET    /api/admin/stats/users             # 用户统计
GET    /api/admin/stats/content           # 内容统计

## 系统配置
GET    /api/admin/boards                  # 板块列表
POST   /api/admin/boards                  # 创建板块
PUT    /api/admin/boards/{id}            # 更新板块
DELETE /api/admin/boards/{id}            # 删除板块
GET    /api/admin/sensitive-words         # 敏感词列表
POST   /api/admin/sensitive-words         # 添加敏感词
PUT    /api/admin/sensitive-words/{id}   # 更新敏感词
DELETE /api/admin/sensitive-words/{id}   # 删除敏感词

## 管理员通知发布
POST   /api/admin/notification/send       # 发送通知（广播/私信）
GET    /api/admin/notification/list       # 获取通知列表
GET    /api/admin/notification/unread-count # 未读数量
PUT    /api/admin/notification/{id}/read # 标记已读

## 操作日志
GET    /api/admin/logs                    # 操作日志列表
```

### 管理员业务流程

```
1. 用户管理流程：
   管理员登录 → 查看用户列表 → 搜索/筛选用户 → 设置角色/禁用状态

2. 内容举报处理流程：
   用户提交举报 → 举报进入待处理队列 → 管理员审核 → 处理结果(删除内容/警告用户/驳回)

3. 敏感词过滤流程：
   管理员设置敏感词及替换词 → 用户发帖/评论时自动检测 → 敏感词替换为指定内容

4. 赛事/群聊管理流程：
   管理员查看赛事/群聊列表 → 修改状态/更换群主/解散违规内容
```

### 举报状态流转

```
PENDING(待处理) → HANDLED(已处理)：确认举报，删除内容或警告用户
PENDING(待处理) → REJECTED(已驳回)：举报不实，驳回举报
```

### 管理员操作日志

所有管理员操作自动记录到 admin_log 表：
- 记录操作类型、目标对象、操作详情、IP地址
- 用于审计追溯和异常排查

---

## 知识图谱功能规划

### 概念设计

知识图谱将构建排球领域的结构化知识网络，包括：
- **实体**：球员、球队、赛事、场地、装备、技术动作
- **关系**：球员-效力-球队、球队-参加-赛事、球员-使用-装备
- **属性**：球员身高、体重、位置；球队战绩、排名

### 数据模型

#### 实体类型 (Entity Type)
| 类型 | 说明 | 示例 |
|------|------|------|
| PLAYER | 球员 | 朱婷、袁心玥 |
| TEAM | 球队 | 中国女排 |
| EVENT | 赛事 | 世界杯、世锦赛 |
| VENUE | 场地 | 北京体育馆 |
| EQUIPMENT | 装备 | 排球、排球鞋 |
| TECHNIQUE | 技术 | 扣球、拦网、传球 |

#### 关系类型 (Relation Type)
| 关系 | 说明 | 示例 |
|------|------|------|
| PLAYS_FOR | 效力于 | 朱婷-效力于-中国女排 |
| PARTICIPATES_IN | 参加 | 中国女排-参加-世界杯 |
| LOCATED_AT | 位于 | 北京体育馆-位于-北京 |
| USES | 使用 | 朱婷-使用-Nike排球鞋 |
| WINS | 获胜 | 中国女排-获胜-奥运会 |
| TEACHES | 执教 | 郎平-执教-中国女排 |

#### 实体表 (knowledge_entity)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 实体名称 |
| type | VARCHAR(20) | 实体类型 |
| description | TEXT | 描述 |
| properties | JSON | 属性JSON |
| created_by | BIGINT | 创建者ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### 关系表 (knowledge_relation)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| from_entity_id | BIGINT | 起始实体ID |
| relation_type | VARCHAR(50) | 关系类型 |
| to_entity_id | BIGINT | 目标实体ID |
| weight | FLOAT | 权重/置信度 |
| created_at | DATETIME | 创建时间 |

#### 实体图片表 (entity_image)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| entity_id | BIGINT | 实体ID |
| image_url | VARCHAR(500) | 图片URL |
| caption | VARCHAR(255) | 图片说明 |

### API 接口规划

```
# 知识图谱接口 /api/knowledge/*

## 实体管理
GET    /api/knowledge/entity/{id}     # 获取实体详情
GET    /api/knowledge/entity/search  # 搜索实体
POST   /api/knowledge/entity          # 创建实体(需登录)
PUT    /api/knowledge/entity/{id}      # 更新实体(需登录)

## 关系管理
GET    /api/knowledge/relation/{id}   # 获取关系详情
POST   /api/knowledge/relation         # 创建关系(需登录)
DELETE /api/knowledge/relation/{id}   # 删除关系(需登录)

## 图谱查询
GET    /api/knowledge/graph           # 获取实体图谱(某实体及其关联)
GET    /api/knowledge/graph/player/{id}   # 获取球员图谱
GET    /api/knowledge/graph/team/{id}     # 获取球队图谱
GET    /api/knowledge/path?from={id}&to={id} # 查询两实体间路径

## 知识浏览
GET    /api/knowledge/types           # 获取所有实体类型
GET    /api/knowledge/type/{type}     # 按类型浏览实体
GET    /api/knowledge/featured        # 精选推荐
```

### 技术实现

```
1. 实体抽取：从帖子/评论中自动抽取排球相关实体(球员名、球队名等)
2. 关系推理：基于用户行为推断实体关系(同队球员、共参加赛事等)
3. 图数据库：可选用 Neo4j 存储图数据，或使用 MySQL + 邻接表
4. 搜索优化：ElasticSearch 索引实体名称和描述
5. 前端展示：图谱可视化(节点-边网络图)
```

---

## 未来功能展望

### 短期规划
- 管理员后台开发
- 内容举报/审核系统
- 数据统计面板

### 中期规划
- 知识图谱基础功能
- 智能推荐(推荐赛事/用户/内容)
- 全文搜索优化

### 长期愿景
- AI辅助问答(排球知识问答)
- 赛事预测/分析
- 虚拟比赛解说
- 排球教学视频库
