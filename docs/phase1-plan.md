# 阶段一：项目骨架 & 用户系统（MVP）

**目标**：能跑通、有基本用户体系

---

## 数据库配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/volleyball_community
spring.datasource.username=root
spring.datasource.password=123456
```

---

## 1. 数据库设计

### user 用户表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| bio | VARCHAR(255) | 个人简介 |
| created_at | DATETIME | 创建时间 |

### board 板块表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| name | VARCHAR(50) | 板块名称 |
| description | VARCHAR(255) | 板块描述 |
| icon | VARCHAR(255) | 板块图标 |
| created_at | DATETIME | 创建时间 |

**初始数据**：
1. 技术讨论
2. 赛事资讯
3. 装备评测
4. 约球专区

### post 帖子表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| title | VARCHAR(100) | 标题 |
| content | TEXT | 内容（支持Markdown） |
| user_id | BIGINT (FK) | 发帖用户ID |
| board_id | BIGINT (FK) | 所属板块ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

---

## 2. 技术选型

- **密码加密**：BCrypt
- **JWT 有效期**：30 天
- **分页**：page=0, size=10（默认）
- **内容格式**：支持 Markdown

---

## 3. 项目结构

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── config/
│   └── SecurityConfig.java          # 安全配置
├── controller/
│   ├── AuthController.java           # 认证
│   ├── UserController.java           # 用户
│   ├── BoardController.java          # 板块
│   └── PostController.java           # 帖子
├── entity/
│   ├── User.java
│   ├── Board.java
│   └── Post.java
├── repository/
│   ├── UserRepository.java
│   ├── BoardRepository.java
│   └── PostRepository.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── BoardService.java
│   └── PostService.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── ...
├── util/
│   └── JwtUtil.java                  # JWT工具类
└── VolleyballCommunityBackendApplication.java
```

---

## 4. 任务清单

- [x] 配置 pom.xml（添加依赖）
- [x] 配置数据库连接
- [x] 创建实体类（User, Board, Post）
- [x] 创建 Repository
- [x] 创建 DTO 类
- [x] 创建 JWT 工具类
- [x] 配置 Security
- [x] 实现 AuthService + AuthController
- [x] 实现 UserService + UserController
- [x] 实现 BoardService + BoardController
- [x] 实现 PostService + PostController
- [x] 初始化板块数据
- [x] 测试接口
