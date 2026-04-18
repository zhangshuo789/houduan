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

### 阶段四：赛事日历 🔄 开发中
- [x] 赛事发布（event）
- [x] 赛事订阅（event_subscription）
