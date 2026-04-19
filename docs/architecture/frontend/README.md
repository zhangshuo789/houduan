# 排球社区前端技术架构文档

## 项目概述

**项目名称**: volleyball-community-frontend
**技术栈**: Vue 3.5.31 + Vue Router 4.6.4 + Vite 5.4.0
**构建工具**: Vite
**状态管理**: Vue 3 内置响应式系统 (ref/computed)
**UI**: 自定义CSS组件
**端口**: 开发服务器 5000

---

## 目录结构

```
docs/architecture/frontend/
├── README.md                    # 本文档
├── 01-project-overview.md       # 项目概述
├── 02-directory-structure.md     # 目录结构
├── 03-routing-module.md         # 路由模块
├── 04-api-module.md             # API接口模块
├── 05-auth-module.md            # 认证模块
├── 06-user-module.md            # 用户模块
├── 07-post-module.md            # 帖子模块
├── 08-comment-module.md         # 评论模块
├── 09-like-favorite-module.md   # 点赞收藏模块
├── 10-follow-module.md          # 关注模块
├── 11-message-module.md         # 私信消息模块
├── 12-group-module.md          # 群组模块
├── 13-event-module.md           # 活动模块
├── 14-sse-module.md            # SSE实时通知模块
├── 15-admin-module.md          # 管理后台模块
├── 16-state-management.md       # 状态管理
├── 17-components.md           # 公共组件
└── 18-css-system.md            # CSS样式系统
```

---

## 技术架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         Vue 3 应用                               │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │   Views     │  │ Components  │  │   Router     │            │
│  │  (页面组件)   │  │ (可复用组件)  │  │  (路由配置)   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│         │                │                │                     │
│         └────────────────┼────────────────┘                     │
│                          ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Composables (Hooks)                       ││
│  │                 useApi.js | useToast.js                      ││
│  └─────────────────────────────────────────────────────────────┘│
│                          │                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                      API Service Layer                       ││
│  │           src/api/index.js (统一封装所有API)                  ││
│  └─────────────────────────────────────────────────────────────┘│
│                          │                                      │
│                          ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                   后端 API (localhost:8080)                  ││
│  │              /api/auth, /api/user, /api/post...             ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## 核心技术组件

### 1. API封装层 (`src/api/index.js`)

**功能**: 统一封装所有后端API请求，自动处理认证头和错误提示

**代码流转**:
```
组件调用 API函数 (如 post.getDetail(1))
    ↓
API函数内部调用 request()
    ↓
request() 添加 Authorization: Bearer {token}
    ↓
fetch 发送请求到后端
    ↓
后端返回 JSON { code, message, data }
    ↓
code !== 200 → 抛出错误 → Toast显示错误信息
    ↓
code === 200 → 返回 data
```

### 2. Toast通知系统

**架构**:
```
组件调用 useToast().success("操作成功")
    ↓
toastBus.emit("success", message, duration)
    ↓
Toast组件监听 toastBus.on(callback)
    ↓
显示 Toast 通知
```

### 3. SSE实时连接

```
App.vue onMounted → connectSSE()
    ↓
new EventSource(`/api/sse/connect?token=${token}`)
    ↓
监听事件: message, group_message, event_update, registration
    ↓
收到事件 → 更新对应组件状态
```

---

## 模块索引

| 模块 | 描述 | 核心文件 |
|------|------|---------|
| [01-项目概述](01-project-overview.md) | 技术栈、依赖、配置 | package.json, vite.config.js |
| [02-目录结构](02-directory-structure.md) | 源代码目录组织 | src/ |
| [03-路由模块](03-routing-module.md) | 路由配置和页面 | src/router/index.js |
| [04-API接口模块](04-api-module.md) | API封装和调用 | src/api/index.js |
| [05-认证模块](05-auth-module.md) | 登录注册 | Login.vue, Register.vue |
| [06-用户模块](06-user-module.md) | 用户信息管理 | UserProfile.vue, EditProfile.vue |
| [07-帖子模块](07-post-module.md) | 帖子浏览和发布 | PostDetail.vue, CreatePost.vue |
| [08-评论模块](08-comment-module.md) | 评论功能 | PostDetail.vue内 |
| [09-点赞收藏](09-like-favorite-module.md) | 点赞和收藏 | PostDetail.vue内 |
| [10-关注模块](10-follow-module.md) | 用户关注 | UserProfile.vue |
| [11-私信消息](11-message-module.md) | 私聊功能 | Messages.vue, Chat.vue |
| [12-群组模块](12-group-module.md) | 群聊功能 | Groups.vue, GroupChat.vue |
| [13-活动模块](13-event-module.md) | 活动浏览和报名 | Events.vue, EventDetail.vue |
| [14-SSE实时通知](14-sse-module.md) | 实时推送 | App.vue, src/api/index.js |
| [15-管理后台](15-admin-module.md) | 管理员功能 | AdminDashboard.vue等 |
| [16-状态管理](16-state-management.md) | 响应式状态 | ref, computed |
| [17-公共组件](17-components.md) | 可复用组件 | NavBar.vue, Toast.vue |
| [18-CSS样式系统](18-css-system.md) | 样式规范 | assets/styles/*.css |
