# 13 活动模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `Events.vue` | 活动列表页 |
| `EventDetail.vue` | 活动详情页 |
| `CreateEvent.vue` | 创建活动页 |
| `MySubscriptions.vue` | 我的订阅页 |
| `src/api/index.js` | event API封装 |

## 活动列表 `Events.vue`

### 页面结构

```
┌─────────────────────────────────────────┐
│  活动中心                                │
├─────────────────────────────────────────┤
│  筛选: [全部] [比赛中] [训练] [交流]      │
├─────────────────────────────────────────┤
│  活动列表                               │
│  ┌─────────────────────────────────────┐ │
│  │ [图片] 活动标题                       │ │
│  │        类型: 比赛                    │ │
│  │        时间: 2026-05-01             │ │
│  │        地点: 体育馆                  │ │
│  │        费用: ¥50  已有 10/20 人报名   │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// Events.vue
import { ref, onMounted } from 'vue'
import { event as eventApi } from '@/api'

export default {
  setup() {
    const events = ref([])
    const page = ref(0)
    const size = 10
    const hasMore = ref(true)

    const loadEvents = async () => {
      const res = await eventApi.list(page.value, size)
      events.value = res.data.content
      hasMore.value = !res.data.last
    }

    const loadMore = async () => {
      page.value++
      const res = await eventApi.list(page.value, size)
      events.value.push(...res.data.content)
      hasMore.value = !res.data.last
    }

    onMounted(loadEvents)

    return { events, loadMore, hasMore }
  }
}
```

## 活动详情 `EventDetail.vue`

### 路由

```
路径: /event/:id
参数: id - 活动ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  [返回]                                  │
├─────────────────────────────────────────┤
│  [活动大图]                              │
├─────────────────────────────────────────┤
│  活动标题                                │
│  类型: 比赛  状态: 报名中               │
├─────────────────────────────────────────┤
│  📅 2026-05-01 09:00 ~ 17:00            │
│  📍 体育馆                               │
│  👤 组织者: xxx                          │
│  💰 费用: ¥50/人                         │
│  👥 人数: 10/20 已报名                  │
├─────────────────────────────────────────┤
│  活动描述                                │
│  这里是活动的详细描述内容...              │
├─────────────────────────────────────────┤
│  [订阅活动]  (不占名额，接收通知)        │
│  [立即报名]  (占名额)                    │
├─────────────────────────────────────────┤
│  报名列表 (如果已登录且是组织者可见)     │
│  ┌─────────────────────────────────────┐ │
│  │ 用户1 - 队伍: 排球一队 - 待审核       │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// EventDetail.vue
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { event as eventApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const router = useRouter()
    const toast = useToast()

    const event = ref(null)
    const isSubscribed = ref(false)
    const registrations = ref([])
    const showRegisterDialog = ref(false)
    const registerForm = ref({
      teamName: '',
      contactPerson: '',
      contactPhone: '',
      teamSize: 1
    })

    const isOrganizer = computed(() => {
      const user = getUserFromStorage()
      return user.value && user.value.id === event.value?.organizerId
    })

    const loadEvent = async () => {
      const eventId = route.params.id
      const res = await eventApi.getDetail(eventId)
      event.value = res.data
    }

    const handleSubscribe = async () => {
      const eventId = event.value.id
      if (isSubscribed.value) {
        await eventApi.unsubscribe(eventId)
        isSubscribed.value = false
        toast.success('已取消订阅')
      } else {
        await eventApi.subscribe(eventId)
        isSubscribed.value = true
        toast.success('订阅成功')
      }
    }

    const handleRegister = async () => {
      try {
        await eventApi.register(event.value.id, registerForm.value)
        toast.success('报名成功，请等待审核')
        showRegisterDialog.value = false
        await loadEvent()  // 刷新报名人数
      } catch (error) {
        toast.error(error.message)
      }
    }

    onMounted(async () => {
      await loadEvent()
      // 检查订阅状态
      // 加载报名列表（如果是组织者）
    })

    return { event, isSubscribed, isOrganizer, handleSubscribe, handleRegister, showRegisterDialog, registerForm }
  }
}
```

## 创建活动 `CreateEvent.vue`

### 路由

```
路径: /create-event
```

### 表单结构

```html
<form @submit.prevent="handleSubmit">
  <input v-model="form.title" placeholder="活动标题" required>

  <select v-model="form.type">
    <option value="MATCH">比赛</option>
    <option value="TRAINING">训练</option>
    <option value="ACTIVITY">交流活动</option>
  </select>

  <input type="datetime-local" v-model="form.startTime">
  <input type="datetime-local" v-model="form.endTime">

  <input v-model="form.location" placeholder="活动地点">

  <input type="number" v-model="form.fee" placeholder="费用">

  <input type="number" v-model="form.maxParticipants" placeholder="最大参与人数">

  <textarea v-model="form.description" placeholder="活动描述"></textarea>

  <button type="submit">发布活动</button>
</form>
```

### 代码流转

```javascript
// CreateEvent.vue
const form = ref({
  title: '',
  type: 'ACTIVITY',
  startTime: '',
  endTime: '',
  location: '',
  fee: 0,
  maxParticipants: 20,
  description: ''
})

const handleSubmit = async () => {
  try {
    await eventApi.create(form.value)
    toast.success('活动创建成功')
    router.push('/events')
  } catch (error) {
    toast.error(error.message)
  }
}
```

## API调用

```javascript
// 获取活动列表
eventApi.list(page, size)
  → GET /api/event

// 获取活动详情
eventApi.getDetail(id)
  → GET /api/event/:id

// 创建活动
eventApi.create(data)
  → POST /api/event

// 更新活动
eventApi.update(id, data)
  → PUT /api/event/:id

// 删除活动
eventApi.delete(id)
  → DELETE /api/event/:id

// 订阅活动（关注，不占名额）
eventApi.subscribe(id)
  → POST /api/event/:id/subscribe

// 取消订阅
eventApi.unsubscribe(id)
  → DELETE /api/event/:id/subscribe

// 报名活动（占名额）
eventApi.register(id, data)
  → POST /api/event/:id/register

// 获取活动报名列表
eventApi.getRegistrations(id)
  → GET /api/event/:id/registration

// 审核报名（组织者）
eventApi.reviewRegistration(eventId, regId, approved)
  → PUT /api/event/:eventId/registration/:regId

// 获取用户订阅的活动
eventApi.getSubscriptions(userId)
  → GET /api/user/:userId/subscriptions
```
