# 02 目录结构

## 完整目录结构

```
D:/WebProject/volleyball-community-frontend/
├── src/
│   ├── api/                    # API服务层
│   │   └── index.js           # 所有API封装
│   ├── assets/
│   │   ├── styles/            # CSS样式文件
│   │   │   ├── variables.css  # CSS变量/设计令牌
│   │   │   ├── base.css       # 基础样式重置
│   │   │   ├── components.css # 组件样式
│   │   │   └── animations.css # 动画定义
│   │   ├── main.css          # CSS统一入口
│   │   └── logo.svg          # Logo图片
│   ├── components/           # 可复用Vue组件
│   │   ├── icons/            # 图标组件
│   │   │   ├── IconCommunity.vue
│   │   │   ├── IconDocumentation.vue
│   │   │   ├── IconEcosystem.vue
│   │   │   ├── IconSupport.vue
│   │   │   └── IconTooling.vue
│   │   ├── NavBar.vue        # 导航栏组件
│   │   ├── Toast.vue         # Toast通知组件
│   │   ├── HelloWorld.vue    # 示例组件
│   │   ├── TheWelcome.vue    # 欢迎组件
│   │   └── WelcomeItem.vue   # 欢迎项组件
│   ├── composables/          # Vue Composables (类Hook)
│   │   ├── useApi.js         # API封装Hook
│   │   └── useToast.js       # Toast Hook
│   ├── config/
│   │   └── index.js          # 环境配置读取
│   ├── router/
│   │   └── index.js          # 路由配置
│   ├── utils/
│   │   └── toast.js          # Toast事件总线
│   ├── views/               # 页面组件
│   │   ├── Home.vue          # 首页
│   │   ├── Login.vue         # 登录页
│   │   ├── Register.vue      # 注册页
│   │   ├── Board.vue         # 板块页
│   │   ├── PostDetail.vue    # 帖子详情页
│   │   ├── CreatePost.vue    # 创建/编辑帖子页
│   │   ├── UserProfile.vue   # 用户主页
│   │   ├── EditProfile.vue   # 编辑资料页
│   │   ├── Messages.vue      # 消息列表页
│   │   ├── Chat.vue          # 私聊页
│   │   ├── Groups.vue        # 群组列表页
│   │   ├── GroupChat.vue     # 群聊页
│   │   ├── Events.vue        # 活动列表页
│   │   ├── EventDetail.vue   # 活动详情页
│   │   ├── CreateEvent.vue   # 创建活动页
│   │   ├── MySubscriptions.vue # 我的订阅页
│   │   ├── AdminDashboard.vue # 管理员首页
│   │   ├── AdminUsers.vue    # 用户管理页
│   │   ├── AdminReports.vue  # 举报管理页
│   │   ├── AdminEvents.vue   # 活动管理页
│   │   ├── AdminGroups.vue   # 群组管理页
│   │   └── AdminSettings.vue # 系统设置页
│   ├── App.vue               # 根组件
│   └── main.js              # 应用入口
├── public/
├── index.html
├── vite.config.js
├── package.json
├── .env.example
└── jsconfig.json
```

## 目录职责说明

### `src/api/`

API服务层，封装所有与后端的HTTP通信。

**核心文件**: `src/api/index.js`
- 包含 auth, user, board, post, comment, file, follow, message, group, event, admin 等API模块
- 提供统一的错误处理和Toast提示
- 管理认证Token和用户状态

### `src/components/`

可复用的Vue组件，不包含业务逻辑，仅做展示。

| 组件 | 用途 |
|------|------|
| NavBar | 顶部导航栏，含Logo、菜单、用户下拉框 |
| Toast | 全局Toast通知，使用Teleport渲染到body |
| Icon* | SVG图标组件 |

### `src/composables/`

Vue 3 Composition API的封装，类似于React Hooks。

| Hook | 功能 |
|------|------|
| useApi | 包装API调用，自动处理错误和Toast |
| useToast | 提供success/error/warning/info方法 |

### `src/views/`

页面级组件，对应路由。每个文件是一个完整页面。

### `src/utils/`

工具函数和事件总线。

| 文件 | 功能 |
|------|------|
| toast.js | Toast的EventEmitter实现 |

### `src/config/`

读取环境变量，封装为配置对象。

### `src/router/`

Vue Router路由配置，定义URL与组件的映射。

### `src/assets/styles/`

全局CSS样式。

| 文件 | 功能 |
|------|------|
| variables.css | CSS自定义属性（颜色、字体、间距等） |
| base.css | 样式重置和基础定义 |
| components.css | 通用组件样式（按钮、表单、卡片等） |
| animations.css | 动画关键帧定义 |
| main.css | 统一入口，导入所有样式 |
