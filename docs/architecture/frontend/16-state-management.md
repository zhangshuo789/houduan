# 16 状态管理

## 概述

本项目**未使用** Vuex/Pinia 等状态管理库，而是使用 Vue 3 内置的响应式系统：
- `ref()` - 创建响应式基本类型和对象
- `computed()` - 创建计算属性
- `watch()` - 监听响应式数据变化

## 用户状态管理

### 存储位置

```javascript
// src/api/index.js

let currentUser = ref(null)

// 初始化时从 localStorage 恢复
export function getUserFromStorage() {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  }
  return currentUser
}

// 设置认证
export function setAuth(token, user) {
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
  currentUser.value = user
}

// 清除认证
export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  currentUser.value = null
}
```

### 在组件中使用

```javascript
import { currentUser, getUserFromStorage } from '@/api'

export default {
  setup() {
    // 获取用户
    const user = getUserFromStorage()
    console.log(user.value)  // { id, username, nickname, ... }

    // 响应式监听
    watch(currentUser, (newUser) => {
      console.log('用户信息变化:', newUser)
    })

    return { user }
  }
}
```

## Token管理

### 存储

```javascript
// 登录成功后
localStorage.setItem('token', token)
```

### 读取

```javascript
// API请求时
const token = localStorage.getItem('token')
if (token) {
  headers['Authorization'] = `Bearer ${token}`
}
```

### 清除

```javascript
// 登出时
localStorage.removeItem('token')
```

## 组件内状态

### 本地状态 (ref)

```javascript
import { ref } from 'vue'

export default {
  setup() {
    const count = ref(0)
    const message = ref('')
    const user = ref({ id: 1, name: '' })

    const increment = () => {
      count.value++
    }

    return { count, message, user, increment }
  }
}
```

### 计算属性 (computed)

```javascript
import { ref, computed } from 'vue'

export default {
  setup() {
    const firstName = ref('John')
    const lastName = ref('Doe')

    const fullName = computed(() => {
      return `${firstName.value} ${lastName.value}`
    })

    const isAdult = computed(() => {
      return age.value >= 18
    })

    return { firstName, lastName, fullName, isAdult }
  }
}
```

### 监听变化 (watch)

```javascript
import { ref, watch } from 'vue'

export default {
  setup() {
    const searchQuery = ref('')

    // 监听 searchQuery 变化，执行搜索
    watch(searchQuery, async (newQuery) => {
      if (newQuery.length > 2) {
        const results = await searchAPI(newQuery)
        searchResults.value = results
      }
    })

    return { searchQuery }
  }
}
```

## 跨组件状态共享

### 方式一：Prop Drilling

```html
<!-- 父组件 -->
<ChildComponent :user="user" @update="handleUpdate" />

<!-- 子组件 -->
<GrandchildComponent :user="user" @update="handleUpdate" />
```

### 方式二：Provide/Inject

```javascript
// 父组件
import { provide, ref } from 'vue'

export default {
  setup() {
    const theme = ref('dark')
    provide('theme', theme)

    const updateTheme = (newTheme) => {
      theme.value = newTheme
    }
    provide('updateTheme', updateTheme)
  }
}

// 子组件
import { inject } from 'vue'

export default {
  setup() {
    const theme = inject('theme')
    const updateTheme = inject('updateTheme')

    return { theme, updateTheme }
  }
}
```

## SSE连接状态

```javascript
// src/api/index.js
let eventSource = null

export function connectSSE(...) {
  // 连接管理
  eventSource = new EventSource(...)
}

export function disconnectSSE() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}
```

## Toast通知状态

使用事件总线模式，无需全局状态：

```javascript
// src/utils/toast.js
class ToastEventBus {
  constructor() {
    this.listeners = []
  }

  on(callback) {
    this.listeners.push(callback)
  }

  emit(message, type, duration) {
    this.listeners.forEach(cb => cb(message, type, duration))
  }

  success(message, duration = 3000) {
    this.emit(message, 'success', duration)
  }

  error(message, duration = 3000) {
    this.emit(message, 'error', duration)
  }
}

export const toastBus = new ToastEventBus()
```

## 表单状态

```javascript
import { ref, reactive } from 'vue'

// 方式一：分别定义
const username = ref('')
const password = ref('')

// 方式二：使用 reactive
const form = reactive({
  username: '',
  password: '',
  remember: false
})

// 方式三：封装为 composable
// src/composables/useForm.js
export function useForm(initialData) {
  const form = reactive({ ...initialData })

  const reset = () => {
    Object.keys(initialData).forEach(key => {
      form[key] = initialData[key]
    })
  }

  const clear = () => {
    Object.keys(form).forEach(key => {
      if (typeof form[key] === 'string') form[key] = ''
      if (typeof form[key] === 'boolean') form[key] = false
      if (typeof form[key] === 'number') form[key] = 0
    })
  }

  return { form, reset, clear }
}
```

## 列表分页状态

```javascript
import { ref, computed } from 'vue'

const page = ref(0)
const size = 10
const totalElements = ref(0)
const list = ref([])

const totalPages = computed(() => Math.ceil(totalElements.value / size))
const hasMore = computed(() => page.value < totalPages.value - 1)

const nextPage = () => {
  if (hasMore.value) {
    page.value++
    loadData()
  }
}

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    loadData()
  }
}
```
