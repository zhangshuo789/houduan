# 14 SSE实时通知模块

## 概述

SSE（Server-Sent Events）实现后端到前端的实时推送，用于：
- 新私信通知
- 群组新消息
- 活动更新通知
- 报名审核结果

## 核心文件

| 文件 | 描述 |
|------|------|
| `App.vue` | SSE连接初始化 |
| `src/api/index.js` | SSE连接函数 |

## 代码流转

### SSE连接初始化

```javascript
// App.vue
import { onMounted, onUnmounted, ref } from 'vue'
import { getUserFromStorage, disconnectSSE } from '@/api'

let eventSource = null

onMounted(() => {
  const user = getUserFromStorage()
  if (user.value) {
    connectSSE()
  }
})

onUnmounted(() => {
  disconnectSSE()
})
```

### 连接函数实现

```javascript
// src/api/index.js

let eventSource = null

export function connectSSE(
  onMessage,
  onGroupMessage,
  onEventUpdate,
  onEventStatusChanged,
  onNewRegistration,
  onRegistrationResult
) {
  const token = localStorage.getItem('token')
  if (!token) return

  // EventSource不支持自定义headers，token放在query参数中
  eventSource = new EventSource(`${sseUrl}/api/sse/connect?token=${token}`)

  // 监听私信
  eventSource.addEventListener('message', (event) => {
    const data = JSON.parse(event.data)
    onMessage && onMessage(data)
  })

  // 监听群消息
  eventSource.addEventListener('group_message', (event) => {
    const data = JSON.parse(event.data)
    onGroupMessage && onGroupMessage(data)
  })

  // 监听活动更新
  eventSource.addEventListener('event_update', (event) => {
    const data = JSON.parse(event.data)
    onEventUpdate && onEventUpdate(data)
  })

  // 监听报名状态变更
  eventSource.addEventListener('registration', (event) => {
    const data = JSON.parse(event.data)
    onNewRegistration && onNewRegistration(data)
  })

  eventSource.onerror = () => {
    console.log('SSE连接断开，5秒后重连')
    eventSource.close()
    setTimeout(() => {
      if (localStorage.getItem('token')) {
        connectSSE(onMessage, onGroupMessage, onEventUpdate, onEventStatusChanged, onNewRegistration, onRegistrationResult)
      }
    }, 5000)
  }
}

export function disconnectSSE() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}
```

### 事件类型和处理

| 事件名 | 触发场景 | 前端处理 |
|--------|---------|---------|
| `message` | 收到新私信 | 显示Toast通知，更新消息列表 |
| `group_message` | 群组新消息 | 更新群聊界面 |
| `event_update` | 活动更新 | 更新活动详情页 |
| `registration` | 新报名通知 | 通知活动组织者 |
| `follow` | 有人关注 | 更新粉丝数提示 |
| `like` | 帖子被点赞 | 更新点赞数提示 |

### 在各页面中使用

```javascript
// Messages.vue
import { onMounted, onUnmounted } from 'vue'
import { connectSSE, disconnectSSE } from '@/api'

export default {
  setup() {
    const handleNewMessage = (msg) => {
      // 如果在消息列表页，刷新会话
      loadConversations()
      // 如果有新的未读，显示通知
      toast.info(`收到来自 ${msg.senderName} 的消息`)
    }

    onMounted(() => {
      connectSSE(handleNewMessage, null, null, null, null, null)
    })

    onUnmounted(() => {
      disconnectSSE()
    })

    return {}
  }
}
```

## SSE连接流程图

```
┌─────────────────────────────────────────────────────────┐
│                    前端 (Vue App)                        │
├─────────────────────────────────────────────────────────┤
│  App.vue onMounted                                      │
│       ↓                                                 │
│  connectSSE()                                           │
│       ↓                                                 │
│  new EventSource('/api/sse/connect?token=xxx')          │
│       ↓                                                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │           EventSource 连接                       │    │
│  │           (HTTP长连接)                          │    │
│  └─────────────────────────────────────────────────┘    │
│                    ↕ ↕ ↕                               │
│              后端 SseService                           │
│                    ↓                                   │
│  用户A发送私信给用户B                                   │
│       ↓                                                 │
│  SseService.sendMessageToUser(B用户ID, "message", msg)  │
│       ↓                                                 │
│  EventSource 推送事件到前端                             │
│       ↓                                                 │
│  前端 onMessage 回调执行                                │
│       ↓                                                 │
│  Toast通知 + 更新UI                                     │
└─────────────────────────────────────────────────────────┘
```

## 注意事项

1. **Token传递**: EventSource不支持自定义headers，token通过URL query参数传递
2. **自动重连**: onerror时会自动在5秒后重连
3. **连接管理**: 登出时需要调用disconnectSSE()关闭连接
4. **内存泄漏**: 组件销毁时必须断开连接
