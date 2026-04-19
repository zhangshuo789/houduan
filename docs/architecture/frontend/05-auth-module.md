# 05 认证模块

## 核心文件

| 文件 | 组件 | 描述 |
|------|------|------|
| `Login.vue` | 视图 | 登录页面 |
| `Register.vue` | 视图 | 注册页面 |
| `src/api/index.js` | 模块 | 认证API封装 |

## 认证流程

```
┌─────────────────────────────────────────────────────────┐
│                      登录流程                             │
├─────────────────────────────────────────────────────────┤
│  用户输入 (username, password)                            │
│           ↓                                              │
│  Login.vue 调用 auth.login(form)                         │
│           ↓                                              │
│  POST /api/auth/login                                   │
│           ↓                                              │
│  后端验证返回 { token, userId, username, nickname }      │
│           ↓                                              │
│  setAuth(token, user)                                   │
│           ↓                                              │
│  localStorage.setItem('token', token)                   │
│  localStorage.setItem('user', JSON.stringify(user))     │
│  currentUser.value = user                               │
│           ↓                                              │
│  路由跳转: admin用户 → /admin/overview                   │
│           普通用户 → /                                   │
└─────────────────────────────────────────────────────────┘
```

## 代码实现

### 登录页面 `Login.vue`

```javascript
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth, setAuth } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const router = useRouter()
    const toast = useToast()
    const form = ref({ username: '', password: '' })

    const handleLogin = async () => {
      try {
        const res = await auth.login(form.value)
        setAuth(res.data.token, res.data)
        toast.success('登录成功')
        // 根据用户角色跳转
        if (res.data.role === 'ADMIN') {
          router.push('/admin/overview')
        } else {
          router.push('/')
        }
      } catch (error) {
        toast.error(error.message)
      }
    }

    return { form, handleLogin }
  }
}
```

### 注册页面 `Register.vue`

```javascript
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const router = useRouter()
    const toast = useToast()
    const form = ref({ username: '', password: '', nickname: '' })

    const handleRegister = async () => {
      try {
        await auth.register(form.value)
        toast.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        toast.error(error.message)
      }
    }

    return { form, handleRegister }
  }
}
```

## 认证状态管理

### 设置认证

```javascript
// src/api/index.js

let currentUser = ref(null)

export function setAuth(token, user) {
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
  currentUser.value = user
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  currentUser.value = null
}

export function getUserFromStorage() {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  }
  return currentUser
}
```

### 获取认证头

```javascript
export function getHeaders() {
  const token = localStorage.getItem('token')
  const headers = {
    'Content-Type': 'application/json'
  }
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}
```

## 登出流程

```javascript
import { clearAuth } from '@/api'
import { useRouter } from 'vue-router'

const handleLogout = () => {
  clearAuth()
  disconnectSSE()  // 断开SSE连接
  router.push('/login')
}
```

## 页面权限控制

前端**未使用**路由守卫，而是在组件内部判断：

```javascript
// 示例：CreatePost.vue
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserFromStorage } from '@/api'

onMounted(() => {
  const user = getUserFromStorage()
  if (!user.value) {
    router.push('/login')
  }
})
```

## Token存储

- **存储位置**: localStorage
- **Key**: `'token'`
- **格式**: JWT字符串

## 当前用户获取

```javascript
import { currentUser, getUserFromStorage } from '@/api'

// 在组件中使用
const user = getUserFromStorage()
console.log(user.value)  // { id, username, nickname, ... }
```
