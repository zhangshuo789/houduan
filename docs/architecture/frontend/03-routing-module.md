# 03 路由模块

## 路由配置

**文件**: `src/router/index.js`

## 路由表

| 路径 | 组件 | 描述 | 权限 |
|------|------|------|------|
| `/` | `Home.vue` | 首页 | 公开 |
| `/login` | `Login.vue` | 登录页 | 公开 |
| `/register` | `Register.vue` | 注册页 | 公开 |
| `/board/:id` | `Board.vue` | 板块帖子列表 | 公开 |
| `/post/:id` | `PostDetail.vue` | 帖子详情 | 公开 |
| `/create-post` | `CreatePost.vue` | 创建帖子 | 需登录 |
| `/edit-post/:id` | `CreatePost.vue` | 编辑帖子 | 需登录 |
| `/user/:id` | `UserProfile.vue` | 用户主页 | 公开 |
| `/edit-profile` | `EditProfile.vue` | 编辑资料 | 需登录 |
| `/messages` | `Messages.vue` | 私信列表 | 需登录 |
| `/chat/:userId` | `Chat.vue` | 私聊窗口 | 需登录 |
| `/groups` | `Groups.vue` | 群组列表 | 需登录 |
| `/group/:id` | `GroupChat.vue` | 群聊窗口 | 需登录 |
| `/events` | `Events.vue` | 活动列表 | 公开 |
| `/event/:id` | `EventDetail.vue` | 活动详情 | 公开 |
| `/create-event` | `CreateEvent.vue` | 创建活动 | 需登录 |
| `/my-subscriptions` | `MySubscriptions.vue` | 我的订阅 | 需登录 |
| `/admin/overview` | `AdminDashboard.vue` | 管理首页 | 管理员 |
| `/admin/users` | `AdminUsers.vue` | 用户管理 | 管理员 |
| `/admin/reports` | `AdminReports.vue` | 举报管理 | 管理员 |
| `/admin/events` | `AdminEvents.vue` | 活动管理 | 管理员 |
| `/admin/groups` | `AdminGroups.vue` | 群组管理 | 管理员 |
| `/admin/settings` | `AdminSettings.vue` | 系统设置 | 管理员 |

## 代码流转

### 路由跳转流程

```
用户点击链接/调用 router.push('/path')
    ↓
Vue Router 匹配路由表
    ↓
找到匹配的路由记录
    ↓
渲染对应的 View 组件
    ↓
组件 onMounted → 加载数据
```

### 路由懒加载

```javascript
// 示例：Home.vue 懒加载
const Home = () => import('../views/Home.vue')

const routes = [
  { path: '/', component: Home }
]
```

### 动态路由参数

```javascript
// 帖子详情页
{ path: '/post/:id', component: PostDetail }

// PostDetail.vue 中获取参数
const route = useRoute()
const postId = route.params.id
```

### 路由导航

```javascript
// 编程式导航
import { useRouter } from 'vue-router'

const router = useRouter()
router.push('/')           // 首页
router.push(`/post/${id}`) // 帖子详情
router.back()              // 返回上一页
```

## 页面数据加载

每个页面组件在 `onMounted` 时调用API获取数据：

```javascript
// PostDetail.vue 示例
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { post as postApi } from '@/api'

export default {
  setup() {
    const route = useRoute()
    const post = ref(null)

    onMounted(async () => {
      const res = await postApi.getDetail(route.params.id)
      post.value = res.data
    })

    return { post }
  }
}
```
