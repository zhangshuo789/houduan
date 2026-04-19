# 11 私信消息模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `Messages.vue` | 消息列表/会话列表页 |
| `Chat.vue` | 私聊窗口页面 |
| `src/api/index.js` | message API封装 |

## 会话列表 `Messages.vue`

### 页面结构

```
┌─────────────────────────────────────────┐
│  消息中心                    [刷新]       │
├─────────────────────────────────────────┤
│  未读消息提示 (如有)                      │
├─────────────────────────────────────────┤
│  会话列表                               │
│  ┌─────────────────────────────────────┐ │
│  │ [头像] 用户昵称                      │ │
│  │        最后一条消息预览...   时间     │ │
│  │        未读数: 3                     │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// Messages.vue
import { ref, onMounted } from 'vue'
import { message as messageApi } from '@/api'
import { useRouter } from 'vue-router'

export default {
  setup() {
    const router = useRouter()
    const conversations = ref([])
    const unreadCount = ref(0)

    const loadConversations = async () => {
      const res = await messageApi.getConversations()
      conversations.value = res.data
    }

    const loadUnreadCount = async () => {
      const res = await messageApi.getUnreadCount()
      unreadCount.value = res.data.unreadCount
    }

    const enterChat = (userId) => {
      router.push(`/chat/${userId}`)
    }

    onMounted(() => {
      loadConversations()
      loadUnreadCount()
    })

    return { conversations, unreadCount, enterChat }
  }
}
```

## 私聊页面 `Chat.vue`

### 路由

```
路径: /chat/:userId
参数: userId - 聊天对象用户ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  [返回]  与 xxx 的聊天                    │
├─────────────────────────────────────────┤
│                                         │
│  消息列表 (滚动区域)                      │
│  ┌─────────────────────────────────────┐ │
│  │        对方消息 (左对齐)              │ │
│  └─────────────────────────────────────┘ │
│  ┌─────────────────────────────────────┐ │
│  │              我的消息 (右对齐)        │ │
│  └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│  [输入框________________________] [发送] │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// Chat.vue
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message as messageApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const router = useRouter()
    const toast = useToast()

    const chatUser = ref(null)       // 聊天对象用户信息
    const messages = ref([])          // 消息列表
    const newMessage = ref('')        // 输入的消息
    const messageListRef = ref(null)  // 消息列表DOM引用

    const loadChatUser = async () => {
      const userId = route.params.userId
      const userRes = await userApi.getInfo(userId)
      chatUser.value = userRes.data
    }

    const loadMessages = async () => {
      const userId = route.params.userId
      const res = await messageApi.getPrivateMessages(userId, 0, 50)
      messages.value = res.data.content

      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim()) return

      const userId = route.params.userId
      try {
        await messageApi.sendPrivateMessage(userId, newMessage.value)
        newMessage.value = ''

        // 添加我的消息到列表
        const myMsg = {
          senderId: getUserFromStorage().value.id,
          content: newMessage.value,
          createdAt: new Date().toISOString()
        }
        messages.value.push(myMsg)

        // 滚动到底部
        await nextTick()
        scrollToBottom()
      } catch (error) {
        toast.error('发送失败')
      }
    }

    const scrollToBottom = () => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    }

    onMounted(async () => {
      await loadChatUser()
      await loadMessages()

      // 标记消息为已读
      await messageApi.markAsRead(route.params.userId)
    })

    return { chatUser, messages, newMessage, sendMessage, messageListRef }
  }
}
```

## SSE实时消息

消息接收通过SSE实现，参见 [14-SSE实时通知模块](14-sse-module.md)

```javascript
// App.vue 中监听
connectSSE(
  (data) => {  // onMessage - 收到私信
    // 如果是当前聊天窗口的消息，添加到列表
    if (data.senderId === currentChatUserId) {
      messages.value.push(data)
    }
    // 更新会话列表未读数
    loadConversations()
  }
)
```

## API调用

```javascript
// 获取会话列表
messageApi.getConversations()
  → GET /api/message/conversations

// 获取与某用户的私信历史
messageApi.getPrivateMessages(userId, page, size)
  → GET /api/message/private/:userId

// 发送私信
messageApi.sendPrivateMessage(userId, content)
  → POST /api/message/private/:userId

// 标记消息已读
messageApi.markAsRead(conversationWithUserId)
  → POST /api/message/read

// 获取未读消息数
messageApi.getUnreadCount()
  → GET /api/message/unread-count
```

## 响应数据结构

### 会话列表

```json
{
  "content": [
    {
      "user": {
        "id": 2,
        "nickname": "用户B",
        "avatar": "http://..."
      },
      "lastMessage": {
        "content": "最后一条消息",
        "createdAt": "2026-04-19T10:00:00"
      },
      "unreadCount": 3
    }
  ]
}
```

### 消息列表

```json
{
  "content": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "content": "你好！",
      "createdAt": "2026-04-19T10:00:00"
    }
  ]
}
```
