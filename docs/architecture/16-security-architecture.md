# 16 安全架构设计

## 整体安全架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端请求                                │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     JwtAuthenticationFilter                     │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ 1. 从 Header 提取 Authorization: Bearer <token>            │ │
│  │ 2. 验证 JWT 有效性                                          │ │
│  │ 3. 加载用户角色 (从 SysUserRole 表)                         │ │
│  │ 4. 设置 SecurityContext                                    │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Security Filter Chain                       │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ permitAll: /api/auth/**, /api/boards/**, /api/file/upload │ │
│  │ permitAll: GET /api/post/**, GET /api/user/**, GET /event  │ │
│  │ hasRole("ADMIN"): /api/admin/**                            │ │
│  │ authenticated: 其他所有接口                                  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Controller Layer                          │
│  从 SecurityContextHolder 获取当前用户信息                       │
└─────────────────────────────────────────────────────────────────┘
```

## JWT认证机制详解

### Token结构

JWT Token 包含三部分：Header、Payload、Signature

**Header**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload**:
```json
{
  "sub": "1",           // userId
  "username": "user1",
  "iat": 1713500000,    // issued at
  "exp": 1716092000     // expiration (30天后)
}
```

### Token生命周期

```
登录成功
    ↓
JwtUtil.generateToken(userId, username)
    ↓
返回 Token 给客户端
    ↓
客户端存储在 localStorage 或 cookie
    ↓
每次请求附加到 Header
    ↓
服务端验证并提取用户信息
```

### Token刷新策略

当前实现为30天固定过期。如需刷新机制：

1. **简单方案**: 前端在Token即将过期时让用户重新登录
2. **进阶方案**: 提供 `/api/auth/refresh` 接口，用refresh token换取新token

## 密码安全

### BCrypt加密

```java
// 注册时加密
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encodedPassword = encoder.encode(rawPassword);

// 登录时验证
boolean matches = encoder.matches(rawPassword, storedPassword);
```

BCrypt特性：
- 内置盐值（salt），无需单独存储
- 计算耗时可调（默认10轮），抵抗暴力破解
- 相同密码每次加密结果不同（安全）

## 权限控制

### 基于角色的访问控制 (RBAC)

**数据库表**:
- `sys_role`: 角色定义 (USER, ADMIN)
- `sys_user_role`: 用户-角色关联

**Security配置**:
```java
.hasRole("ADMIN")  // 相当于 hasAuthority("ROLE_ADMIN")
```

### 接口级别权限

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/user/{userId}")
public ApiResponse deleteUser(@PathVariable Long userId) {
    // 只有管理员可以删除用户
}
```

## 敏感数据保护

### 日志脱敏 (SensitiveDataFilter)

**脱敏字段**:
- phone (手机号) → `138****5678`
- idNumber (身份证) → `310101****1234`
- password → `******`
- username → 前两位后两位，中间 `***`
- address → `上海市****`
- birthday → `****-**-**`

### 数据传输

- HTTPS 加密传输（生产环境必须）
- 响应中不返回密码字段
- 文件上传前检测文件类型和大小

## SQL注入防护

使用Spring Data JPA的Repository接口，底层自动使用PreparedStatement：

```java
// 安全：参数绑定
UserRepository.findByUsername(username);  // 参数自动绑定

// 避免：字符串拼接构建SQL
@Query("SELECT u FROM User u WHERE u.username = " + username)  // 不推荐
```

## XSS防护

- Spring Boot默认有一定的XSS防护
- 富文本内容建议使用HTML净化库（如OWASP Java HTML Sanitizer）
- 敏感词过滤同时起到XSS防护作用

## CORS配置

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

**生产环境建议**:
- `allowedOriginPatterns` 设置具体域名
- 评估是否真的需要 `allowCredentials(true)`

## 会话管理

当前使用无状态JWT，不使用HttpSession：

```java
http.sessionManagement()
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
```

**优点**: 适合分布式部署，不依赖Session存储
**缺点**: Token泄露后难以单点注销

**如需会话管理**:
- 使用Spring Session + Redis
- 或在Token中加入jti (JWT ID) 黑名单

## 安全配置清单

| 配置项 | 推荐值 |
|-------|-------|
| JWT Secret | 至少256位随机字符串 |
| Token过期时间 | 7-30天 |
| 密码最小长度 | 6-8字符 |
| 文件上传大小 | ≤10MB |
| 敏感词缓存 | 5分钟 |
| CORS | 仅允许可信域名 |
