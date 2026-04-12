# 排球社区后端开发计划

> 目标：打造排球版的"知乎"或"贴吧"

---

## 需求概述

- **内容板块**：技术讨论、赛事资讯、装备评测、约球专区
- **用户系统**：注册登录、个人主页、关注/粉丝
- **互动功能**：发帖、评论、点赞、收藏、私信
- **赛事日历**：管理员可发布重要赛事日程，用户可订阅

---

## 阶段一：项目骨架 & 用户系统（MVP）

**目标**：能跑通、有基本用户体系

### 1.1 数据库设计

| 表名 | 字段 | 说明 |
|------|------|------|
| user | id, username, password, nickname, avatar, bio, created_at | 用户表 |
| board | id, name, description, icon, created_at | 板块表 |
| post | id, title, content, user_id, board_id, created_at, updated_at | 帖子表 |

### 1.2 接口设计

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/register | POST | 注册 |
| /api/auth/login | POST | 登录 |
| /api/user/{id} | GET | 用户信息 |
| /api/user/{id} | PUT | 更新个人信息 |
| /api/boards | GET | 板块列表 |
| /api/boards/{id}/posts | GET | 板块下帖子列表 |
| /api/post | POST | 发帖 |
| /api/post/{id} | GET | 帖子详情 |

### 1.3 初步页面结构

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── config/           # 配置
├── controller/       # 控制器
│   ├── AuthController
│   ├── UserController
│   ├── BoardController
│   └── PostController
├── entity/           # 实体类
│   ├── User
│   ├── Board
│   └── Post
├── repository/       # 数据访问
├── service/          # 业务逻辑
└── util/             # 工具类
```

---

## 阶段二：互动功能

**目标**：评论、点赞、收藏

### 2.1 数据库设计

| 表名 | 字段 | 说明 |
|------|------|------|
| comment | id, content, user_id, post_id, parent_id, created_at | 评论表 |
| like | id, user_id, post_id, created_at | 点赞表 |
| favorite | id, user_id, post_id, created_at | 收藏表 |

### 2.2 接口设计

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/post/{id}/comment | POST | 评论帖子 |
| /api/post/{id}/like | POST | 点赞帖子 |
| /api/post/{id}/favorite | POST | 收藏帖子 |
| /api/user/{id}/favorites | GET | 用户收藏列表 |

---

## 阶段三：社交功能

**目标**：关注系统、私信

### 3.1 数据库设计

| 表名 | 字段 | 说明 |
|------|------|------|
| follow | id, follower_id, following_id, created_at | 关注表 |
| message | id, sender_id, receiver_id, content, read, created_at | 私信表 |

### 3.2 接口设计

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/{id}/follow | POST | 关注用户 |
| /api/user/{id}/unfollow | POST | 取消关注 |
| /api/user/{id}/followers | GET | 粉丝列表 |
| /api/user/{id}/following | GET | 关注列表 |
| /api/message | POST | 发送私信 |
| /api/message/conversation/{userId} | GET | 与某用户的私信记录 |

---

## 阶段四：赛事日历 & 管理功能

**目标**：赛事发布、订阅

### 4.1 数据库设计

| 表名 | 字段 | 说明 |
|------|------|------|
| event | id, title, description, start_time, end_time, location, created_at | 赛事表 |
| event_subscription | id, user_id, event_id, created_at | 赛事订阅表 |

### 4.2 接口设计

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/event | POST | 创建赛事（管理员） |
| /api/event | GET | 赛事列表 |
| /api/event/{id} | GET | 赛事详情 |
| /api/event/{id}/subscribe | POST | 订阅赛事 |
| /api/event/{id}/unsubscribe | POST | 取消订阅 |

---

## 当前阶段

**阶段一：项目骨架 & 用户系统（MVP）**

- [ ] 搭建项目结构
- [ ] 配置数据库连接
- [ ] 创建实体类（User, Board, Post）
- [ ] 实现注册登录
- [ ] 实现用户信息接口
- [ ] 实现板块接口
- [ ] 实现帖子接口
