# 12 群组模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `Groups.vue` | 群组列表页 |
| `GroupChat.vue` | 群聊窗口页面 |
| `src/api/index.js` | group API封装 |

## 群组列表 `Groups.vue`

### 页面结构

```
┌─────────────────────────────────────────┐
│  我的群组                    [创建群组]   │
├─────────────────────────────────────────┤
│  群组列表                               │
│  ┌─────────────────────────────────────┐ │
│  │ [群图标] 群组名称                    │ │
│  │           成员数: 10                 │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// Groups.vue
import { ref, onMounted } from 'vue'
import { group as groupApi } from '@/api'
import { useRouter } from 'vue-router'

export default {
  setup() {
    const router = useRouter()
    const myGroups = ref([])

    const loadMyGroups = async () => {
      // 获取我所在的所有群组
      // 后端可能没有直接接口，前端需要过滤
      const res = await groupApi.list()  // 假设有list接口
      myGroups.value = res.data
    }

    const createGroup = async () => {
      // 弹出创建群组对话框
    }

    const enterGroup = (groupId) => {
      router.push(`/group/${groupId}`)
    }

    onMounted(loadMyGroups)

    return { myGroups, createGroup, enterGroup }
  }
}
```

## 群聊页面 `GroupChat.vue`

### 路由

```
路径: /group/:id
参数: id - 群组ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  [返回]  群组名称           [成员管理]    │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────────┐ │
│  │  群公告/简介                          │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  消息列表 (滚动区域)                      │
│  ┌─────────────────────────────────────┐ │
│  │ [头像] 用户名 时间                    │ │
│  │       消息内容                        │ │
│  └─────────────────────────────────────┘ │
│                                         │
├─────────────────────────────────────────┤
│  [输入框________________________] [发送] │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// GroupChat.vue
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { group as groupApi } from '@/api'
import { connectSSE, disconnectSSE } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const toast = useToast()

    const groupInfo = ref(null)
    const members = ref([])
    const messages = ref([])
    const newMessage = ref('')

    const loadGroupInfo = async () => {
      const groupId = route.params.id
      const infoRes = await groupApi.getInfo(groupId)
      groupInfo.value = infoRes.data

      const membersRes = await groupApi.getMembers(groupId)
      members.value = membersRes.data
    }

    const loadMessages = async () => {
      const groupId = route.params.id
      const res = await groupApi.getMessages(groupId, 0, 50)
      messages.value = res.data.content
    }

    const sendMessage = async () => {
      if (!newMessage.value.trim()) return

      const groupId = route.params.id
      try {
        await groupApi.sendMessage(groupId, newMessage.value)
        newMessage.value = ''
        await loadMessages()
      } catch (error) {
        toast.error('发送失败')
      }
    }

    const onGroupMessage = (msg) => {
      // SSE收到群消息
      if (msg.groupId === Number(route.params.id)) {
        messages.value.push(msg)
      }
    }

    onMounted(async () => {
      await loadGroupInfo()
      await loadMessages()

      // 连接SSE监听群消息
      connectSSE(
        null,           // onMessage - 私信（不用）
        onGroupMessage  // onGroupMessage - 群消息
      )
    })

    onUnmounted(() => {
      // 断开SSE（如果有独立的断开函数）
    })

    return { groupInfo, members, messages, newMessage, sendMessage }
  }
}
```

## 群组成员管理

```javascript
// 添加成员
const addMember = async (userId) => {
  await groupApi.addMember(groupInfo.value.id, userId)
  await loadMembers()
  toast.success('添加成功')
}

// 移除成员
const removeMember = async (userId) => {
  await groupApi.removeMember(groupInfo.value.id, userId)
  await loadMembers()
  toast.success('已移除')
}

// 禁言成员
const banMember = async (userId) => {
  await groupApi.banMember(groupInfo.value.id, userId)
  toast.success('已禁言')
}

// 踢出群组
const kickMember = async (userId) => {
  await groupApi.removeMember(groupInfo.value.id, userId)
  toast.success('已踢出')
}
```

## 创建群组

```javascript
// 假设在 Groups.vue 中
const showCreateDialog = ref(false)
const createForm = ref({ name: '', description: '' })

const handleCreate = async () => {
  try {
    const res = await groupApi.create(createForm.value)
    toast.success('群组创建成功')
    showCreateDialog.value = false
    await loadMyGroups()
    router.push(`/group/${res.data.id}`)
  } catch (error) {
    toast.error(error.message)
  }
}
```

## API调用

```javascript
// 创建群组
groupApi.create(data)
  → POST /api/group

// 获取群组信息
groupApi.getInfo(id)
  → GET /api/group/:id

// 获取群组成员
groupApi.getMembers(id)
  → GET /api/group/:id/members

// 添加群成员
groupApi.addMember(groupId, userId)
  → POST /api/group/:groupId/members?userId=:userId

// 移除群成员
groupApi.removeMember(groupId, userId)
  → DELETE /api/group/:groupId/members/:userId

// 发送群消息
groupApi.sendMessage(groupId, content)
  → POST /api/group/:groupId/messages

// 获取群消息历史
groupApi.getMessages(groupId, page, size)
  → GET /api/group/:groupId/messages

// 禁言成员
groupApi.banMember(groupId, userId)
  → POST /api/group/:groupId/ban/:userId

// 解除禁言
groupApi.unbanMember(groupId, userId)
  → DELETE /api/group/:groupId/unban/:userId

// 离开群组
groupApi.leave(groupId, userId)
  → POST /api/group/:groupId/members/:userId/leave
```
