# 17 公共组件

## 组件列表

| 组件 | 文件 | 描述 |
|------|------|------|
| NavBar | `components/NavBar.vue` | 顶部导航栏 |
| Toast | `components/Toast.vue` | Toast通知组件 |
| HelloWorld | `components/HelloWorld.vue` | 示例组件 |
| TheWelcome | `components/TheWelcome.vue` | 欢迎组件 |
| WelcomeItem | `components/WelcomeItem.vue` | 欢迎项组件 |
| Icon* | `components/icons/*.vue` | SVG图标组件 |

## NavBar 导航栏

### 功能

- Logo和网站名称
- 导航菜单链接
- 用户下拉菜单（登录后显示）
- 搜索框（可选）
- 移动端汉堡菜单

### 页面结构

```html
<nav class="navbar">
  <div class="navbar-left">
    <router-link to="/" class="logo">
      <img src="@/assets/logo.svg" alt="排球社区">
      <span>排球社区</span>
    </router-link>
  </div>

  <div class="navbar-center">
    <router-link to="/">首页</router-link>
    <router-link to="/events">活动</router-link>
    <router-link to="/groups">群组</router-link>
  </div>

  <div class="navbar-right">
    <!-- 已登录 -->
    <template v-if="currentUser">
      <router-link to="/messages">
        <span class="icon">💬</span>
        <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
      </router-link>
      <div class="user-dropdown">
        <img :src="currentUser.avatar" class="avatar">
        <div class="dropdown-menu">
          <router-link :to="`/user/${currentUser.id}`">我的主页</router-link>
          <router-link to="/my-subscriptions">我的订阅</router-link>
          <router-link to="/edit-profile">编辑资料</router-link>
          <router-link v-if="isAdmin" to="/admin/overview">管理后台</router-link>
          <button @click="handleLogout">退出</button>
        </div>
      </div>
    </template>

    <!-- 未登录 -->
    <template v-else>
      <router-link to="/login">登录</router-link>
      <router-link to="/register">注册</router-link>
    </template>
  </div>
</nav>
```

### 代码逻辑

```javascript
// NavBar.vue
import { ref, onMounted } from 'vue'
import { getUserFromStorage, clearAuth } from '@/api'
import { useRouter } from 'vue-router'

export default {
  setup() {
    const router = useRouter()
    const currentUser = getUserFromStorage()
    const unreadCount = ref(0)
    const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

    const handleLogout = () => {
      clearAuth()
      router.push('/login')
    }

    onMounted(() => {
      // 加载未读消息数
      if (currentUser.value) {
        loadUnreadCount()
      }
    })

    return { currentUser, unreadCount, isAdmin, handleLogout }
  }
}
```

## Toast 通知组件

### 功能

- 全局显示成功/错误/警告/信息通知
- 自动消失（默认3秒）
- 支持手动关闭

### 使用方式

```javascript
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const toast = useToast()

    toast.success('操作成功')
    toast.error('操作失败')
    toast.warning('警告')
    toast.info('提示')
  }
}
```

### 组件实现

```html
<!-- Toast.vue -->
<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="toast-container">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="['toast', `toast-${toast.type}`]"
      >
        <span>{{ toast.message }}</span>
        <button @click="removeToast(toast.id)">×</button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script>
import { ref } from 'vue'
import { toastBus } from '@/utils/toast'

export default {
  setup() {
    const toasts = ref([])

    const addToast = (message, type, duration = 3000) => {
      const id = Date.now()
      toasts.value.push({ id, message, type })

      setTimeout(() => {
        removeToast(id)
      }, duration)
    }

    const removeToast = (id) => {
      const index = toasts.value.findIndex(t => t.id === id)
      if (index > -1) {
        toasts.value.splice(index, 1)
      }
    }

    // 监听事件
    toastBus.on((message, type, duration) => {
      addToast(message, type, duration)
    })

    return { toasts, removeToast }
  }
}
</script>

<style>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
}

.toast {
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.toast-success { background: #4CAF50; color: white; }
.toast-error { background: #F44336; color: white; }
.toast-warning { background: #FF9800; color: white; }
.toast-info { background: #2196F3; color: white; }
</style>
```

## Icon 图标组件

SVG图标组件，位于 `components/icons/`

```html
<!-- IconCommunity.vue -->
<template>
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
    <path d="M..."/>
  </svg>
</template>
```

使用示例：
```html
<IconCommunity class="icon" />
```

## Composables

### useApi

```javascript
// src/composables/useApi.js
import { useToast } from './useToast'

export function useApi() {
  const toast = useToast()

  const wrapApi = async (apiFn, ...args) => {
    try {
      return await apiFn(...args)
    } catch (error) {
      toast.error(error.message || '操作失败')
      throw error
    }
  }

  return { wrapApi }
}
```

### useToast

```javascript
// src/composables/useToast.js
import { useToast } from '@/utils/toast'

export function useToast() {
  const toast = {
    success: (msg) => toast.success(msg),
    error: (msg) => toast.error(msg),
    warning: (msg) => toast.warning(msg),
    info: (msg) => toast.info(msg)
  }

  return toast
}
```
