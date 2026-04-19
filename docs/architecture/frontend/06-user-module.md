# 06 用户模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `UserProfile.vue` | 用户主页 |
| `EditProfile.vue` | 编辑资料页 |
| `src/api/index.js` | user API封装 |

## 用户主页 `UserProfile.vue`

### 路由参数

```
路径: /user/:id
参数: id - 用户ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│              用户信息卡片                  │
│  ┌─────┐  昵称                          │
│  │头像│  @username                       │
│  └─────┘  个人简介                       │
│           帖子数 | 粉丝 | 关注           │
│           [关注] [发消息]                │
└─────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────┐
│              帖子列表                     │
│  ┌─────────────────────────────────────┐ │
│  │ 帖子标题                             │ │
│  │ 板块 · 发布时间 · 点赞数 · 评论数      │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// UserProfile.vue
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { user as userApi, follow as followApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const toast = useToast()

    const userInfo = ref(null)
    const stats = ref({})
    const posts = ref([])
    const isFollowing = ref(false)

    const loadUserData = async () => {
      const userId = route.params.id

      // 并行加载用户信息、统计数据、帖子
      const [infoRes, statsRes, postsRes] = await Promise.all([
        userApi.getInfo(userId),
        userApi.getStats(userId),
        userApi.getPosts(userId, 0, 10)
      ])

      userInfo.value = infoRes.data
      stats.value = statsRes.data
      posts.value = postsRes.data.content

      // 检查关注状态
      if (isLoggedIn()) {
        const statusRes = await followApi.getFollowStatus(userId)
        isFollowing.value = statusRes.data.isFollowing
      }
    }

    const handleFollow = async () => {
      const userId = route.params.id
      if (isFollowing.value) {
        await followApi.unfollow(userId)
        isFollowing.value = false
        toast.success('已取消关注')
      } else {
        await followApi.follow(userId)
        isFollowing.value = true
        toast.success('关注成功')
      }
    }

    onMounted(loadUserData)

    return { userInfo, stats, posts, isFollowing, handleFollow }
  }
}
```

## 编辑资料页 `EditProfile.vue`

### 功能

- 修改昵称
- 修改头像（上传文件）
- 修改个人简介

### 代码流转

```javascript
// EditProfile.vue
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { user as userApi, file as fileApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const router = useRouter()
    const toast = useToast()

    const form = ref({
      nickname: '',
      avatar: '',
      bio: ''
    })

    onMounted(async () => {
      // 加载当前用户信息
      const user = getUserFromStorage()
      form.value = {
        nickname: user.value.nickname,
        avatar: user.value.avatar,
        bio: user.value.bio || ''
      }
    })

    const handleAvatarUpload = async (event) => {
      const file = event.target.files[0]
      const formData = new FormData()
      formData.append('file', file)
      formData.append('type', 'avatar')

      const res = await fileApi.upload(formData)
      form.value.avatar = res.data.url
      toast.success('头像上传成功')
    }

    const handleSubmit = async () => {
      const user = getUserFromStorage()
      await userApi.update(user.value.id, form.value)
      // 更新本地存储的用户信息
      setAuth(localStorage.getItem('token'), { ...user.value, ...form.value })
      toast.success('资料更新成功')
      router.push(`/user/${user.value.id}`)
    }

    return { form, handleAvatarUpload, handleSubmit }
  }
}
```

## API调用

```javascript
// 获取用户信息
userApi.getInfo(id)
  → GET /api/user/:id

// 更新用户信息
userApi.update(id, data)
  → PUT /api/user/:id

// 获取用户统计
userApi.getStats(id)
  → GET /api/user/:id/stats

// 获取用户帖子列表
userApi.getPosts(id, page, size)
  → GET /api/user/:id/posts

// 获取用户收藏
userApi.getFavorites(id, page, size)
  → GET /api/user/:id/favorites

// 获取用户动态(Feed)
userApi.getFeed(id, page, size)
  → GET /api/user/:id/feed
```

## 响应数据结构

### 用户信息

```json
{
  "id": 1,
  "username": "user1",
  "nickname": "排球达人",
  "avatar": "http://localhost:8080/api/file/1/resource",
  "bio": "热爱排球运动",
  "createdAt": "2026-01-01T10:00:00"
}
```

### 用户统计

```json
{
  "postCount": 42,
  "commentCount": 128,
  "followerCount": 256,
  "followingCount": 64,
  "likeCount": 1024
}
```
