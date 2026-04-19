# 01 认证模块

## 模块概述

负责用户注册、登录功能，颁发JWT令牌，是整个系统的基础安全模块。

## 核心技术

| 技术 | 说明 |
|------|------|
| Spring Security | 安全框架，提供认证/授权基础设施 |
| JWT (jjwt 0.12.3) | 无状态令牌，用于接口认证 |
| BCryptPasswordEncoder | 密码加密存储 |
| Spring Data JPA | 用户数据持久化 |

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── AuthController.java          # 认证接口
├── service/
│   └── AuthService.java              # 认证业务逻辑
├── repository/
│   └── UserRepository.java          # 用户数据访问
├── entity/
│   └── User.java                    # 用户实体
├── dto/request/
│   ├── LoginRequest.java            # 登录请求
│   └── RegisterRequest.java          # 注册请求
├── dto/response/
│   ├── LoginResponse.java           # 登录响应
│   └── ApiResponse.java             # 统一响应格式
└── util/
    └── JwtUtil.java                  # JWT工具类
```

## 数据表结构

**user表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt加密后的密码 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像URL |
| bio | TEXT | 个人简介 |
| created_at | DATETIME | 创建时间 |

## 代码流转

### 注册流程

```
POST /api/auth/register
    ↓
[RegisterRequest] 请求参数校验
    ↓
AuthService.register()
    ↓
UserRepository.findByUsername(username) 检查用户名是否存在
    ↓ 存在 → 抛出RuntimeException("用户名已存在")
    ↓ 不存在
    ↓
BCryptPasswordEncoder.encode(password) 密码加密
    ↓
new User() 创建用户对象
    ↓
UserRepository.save(user) 持久化到数据库
    ↓
return ApiResponse.success("注册成功")
```

### 登录流程

```
POST /api/auth/login
    ↓
[LoginRequest] 请求参数校验
    ↓
AuthService.login()
    ↓
UserRepository.findByUsername(username) 查找用户
    ↓ 未找到 → 抛出RuntimeException("用户名或密码错误")
    ↓ 找到
    ↓
BCryptPasswordEncoder.matches(password, user.password) 验证密码
    ↓ 密码不匹配 → 抛出RuntimeException("用户名或密码错误")
    ↓ 密码匹配
    ↓
JwtUtil.generateToken(user.id, user.username) 生成JWT
    ↓
return LoginResponse { token, userId, username, nickname }
```

### JWT验证流程（请求拦截）

```
请求进入 → JwtAuthenticationFilter.doFilter()
    ↓
从Header提取 Authorization: Bearer <token>
    ↓ 无Token → 直接放行（公开接口可访问，受保护接口后续被Security拦截）
    ↓ 有Token
    ↓
JwtUtil.validateToken(token)
    ↓ Token无效 → 放行（由Security最终决定是否允许）
    ↓ Token有效
    ↓
JwtUtil.getUserIdFromToken(token) 提取用户ID
    ↓
JwtUtil.getUsernameFromToken(token) 提取用户名
    ↓
UserRepository.findById(userId) 加载用户信息
    ↓
UsernamePasswordAuthenticationToken 设置到SecurityContext
    ↓
FilterChain.doFilter() 继续执行
```

## 接口详情

### POST /api/auth/register 注册

**请求体**:
```json
{
  "username": "string (3-20字符)",
  "password": "string (6-20字符)",
  "nickname": "string (1-50字符)"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

**失败响应** (400):
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

### POST /api/auth/login 登录

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "user1",
    "nickname": "排球爱好者"
  }
}
```

## 配置项

**application.properties**:
```properties
jwt.secret=volleyball-community-secret-key-2026-04-12
jwt.expiration=2592000000  # 30天，单位毫秒
```
