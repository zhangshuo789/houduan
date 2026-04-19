# 15 管理后台模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `AdminDashboard.vue` | 管理首页/统计概览 |
| `AdminUsers.vue` | 用户管理 |
| `AdminReports.vue` | 举报管理 |
| `AdminEvents.vue` | 活动管理 |
| `AdminGroups.vue` | 群组管理 |
| `AdminSettings.vue` | 系统设置 |
| `src/api/index.js` | admin API封装 |

## 路由保护

管理后台路由需要管理员权限：

```javascript
// 管理员路由
{
  path: '/admin/overview',
  component: AdminDashboard
},
{
  path: '/admin/users',
  component: AdminUsers
},
// ...
```

**权限判断**：前端在页面加载时检查用户角色，非管理员重定向

```javascript
// AdminDashboard.vue
onMounted(() => {
  const user = getUserFromStorage()
  if (!user.value || user.value.role !== 'ADMIN') {
    router.push('/')
    return
  }
  loadStats()
})
```

## 管理首页 `AdminDashboard.vue`

### 页面结构

```
┌─────────────────────────────────────────────────────────┐
│  管理后台                                                │
├─────────────────────────────────────────────────────────┤
│  [概览] [用户] [举报] [活动] [群组] [设置]                │
├─────────────────────────────────────────────────────────┤
│  统计数据卡片                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ 用户总数  │ │ 帖子总数  │ │ 活动总数  │ │ 待处理举报│  │
│  │   1,234   │ │   5,678   │ │    89    │ │    12    │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│                                                          │
│  最近操作日志                                            │
│  ┌─────────────────────────────────────────────────────┐│
│  │ 管理员A 于 10:00 删除了帖子《xxx》                  ││
│  │ 管理员B 于 09:30 禁用了用户 xxx                    ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### 代码流转

```javascript
// AdminDashboard.vue
import { ref, onMounted } from 'vue'
import { admin as adminApi } from '@/api'

export default {
  setup() {
    const stats = ref({
      userCount: 0,
      postCount: 0,
      eventCount: 0,
      pendingReports: 0
    })
    const recentLogs = ref([])

    const loadStats = async () => {
      const res = await adminApi.getOverview()
      stats.value = res.data
    }

    const loadLogs = async () => {
      const res = await adminApi.getLogs({ page: 0, size: 10 })
      recentLogs.value = res.data.content
    }

    onMounted(() => {
      loadStats()
      loadLogs()
    })

    return { stats, recentLogs }
  }
}
```

## 用户管理 `AdminUsers.vue`

### 功能

- 分页查看用户列表
- 搜索用户
- 设置用户角色（普通用户/管理员）
- 禁用/启用用户

### 代码流转

```javascript
// AdminUsers.vue
const users = ref([])
const searchForm = ref({ keyword: '', role: '' })

const loadUsers = async (params) => {
  const res = await adminApi.getUsers(params)
  users.value = res.data.content
}

const setUserRole = async (userId, roleId) => {
  await adminApi.setUserRole(userId, roleId)
  toast.success('角色设置成功')
  await loadUsers(getQueryParams())
}

const toggleUserStatus = async (user) => {
  const action = user.disabled ? '启用' : '禁用'
  const reason = prompt(`请输入${action}原因:`)
  if (reason) {
    await adminApi.setUserStatus(user.id, !user.disabled, reason)
    toast.success(`用户已${action}`)
    await loadUsers(getQueryParams())
  }
}
```

## 举报管理 `AdminReports.vue`

### 功能

- 查看所有举报列表
- 查看举报详情
- 处理举报（删除内容/忽略）

### 代码流转

```javascript
// AdminReports.vue
const reports = ref([])

const loadReports = async () => {
  const res = await adminApi.getReports({ page: 0, size: 20 })
  reports.value = res.data.content
}

const handleReport = async (reportId, approved, result) => {
  await adminApi.handleReport(reportId, approved, result)
  toast.success('处理完成')
  await loadReports()
}
```

## 活动管理 `AdminEvents.vue`

### 功能

- 查看所有活动
- 审核活动（通过/下线）
- 查看活动报名列表

```javascript
const updateEventStatus = async (eventId, status) => {
  await adminApi.updateEventStatus(eventId, status)
  toast.success('状态已更新')
}
```

## 群组管理 `AdminGroups.vue`

### 功能

- 查看所有群组
- 解散群组
- 转让群主

```javascript
const dissolveGroup = async (groupId) => {
  if (confirm('确定要解散该群组吗？')) {
    await adminApi.dissolveGroup(groupId)
    toast.success('群组已解散')
    await loadGroups()
  }
}

const changeOwner = async (groupId, newOwnerId) => {
  await adminApi.changeGroupOwner(groupId, newOwnerId)
  toast.success('群主已转让')
}
```

## 系统设置 `AdminSettings.vue`

### 功能

- 板块管理（创建/编辑/删除）
- 敏感词管理（添加/编辑/删除）

### 板块管理

```javascript
const boards = ref([])

const loadBoards = async () => {
  const res = await adminApi.getBoards()
  boards.value = res.data
}

const createBoard = async (data) => {
  await adminApi.createBoard(data)
  toast.success('板块创建成功')
  await loadBoards()
}

const deleteBoard = async (id) => {
  if (confirm('确定删除该板块？')) {
    await adminApi.deleteBoard(id)
    toast.success('板块已删除')
    await loadBoards()
  }
}
```

### 敏感词管理

```javascript
const sensitiveWords = ref([])

const loadSensitiveWords = async () => {
  const res = await adminApi.getSensitiveWords()
  sensitiveWords.value = res.data
}

const addWord = async (word, replacement, level) => {
  await adminApi.addSensitiveWord({ word, replacement, level })
  toast.success('敏感词添加成功')
  await loadSensitiveWords()
}

const updateWord = async (id, data) => {
  await adminApi.updateSensitiveWord(id, data)
  toast.success('敏感词已更新')
}

const deleteWord = async (id) => {
  await adminApi.deleteSensitiveWord(id)
  toast.success('敏感词已删除')
}
```

## API调用

```javascript
// 获取管理概览统计
adminApi.getOverview()
  → GET /api/admin/stats/overview

// 获取用户列表
adminApi.getUsers(params)
  → GET /api/admin/users

// 设置用户角色
adminApi.setUserRole(userId, roleId)
  → PUT /api/admin/users/:userId/role

// 启用/禁用用户
adminApi.setUserStatus(userId, disabled, reason)
  → PUT /api/admin/users/:userId/status

// 获取举报列表
adminApi.getReports(params)
  → GET /api/admin/reports

// 处理举报
adminApi.handleReport(reportId, approved, result)
  → PUT /api/admin/reports/:reportId

// 获取活动列表（管理）
adminApi.getEvents(params)
  → GET /api/admin/events

// 更新活动状态
adminApi.updateEventStatus(eventId, status)
  → PUT /api/admin/events/:eventId/status

// 获取群组列表（管理）
adminApi.getGroups(params)
  → GET /api/admin/groups

// 解散群组
adminApi.dissolveGroup(groupId)
  → DELETE /api/admin/groups/:groupId

// 转让群主
adminApi.changeGroupOwner(groupId, newOwnerId)
  → PUT /api/admin/groups/:groupId/owner

// 获取板块列表
adminApi.getBoards()
  → GET /api/admin/boards

// 创建板块
adminApi.createBoard(data)
  → POST /api/admin/boards

// 删除板块
adminApi.deleteBoard(id)
  → DELETE /api/admin/boards/:id

// 获取敏感词列表
adminApi.getSensitiveWords()
  → GET /api/admin/sensitive-words

// 添加敏感词
adminApi.addSensitiveWord(data)
  → POST /api/admin/sensitive-words

// 更新敏感词
adminApi.updateSensitiveWord(id, data)
  → PUT /api/admin/sensitive-words/:id

// 删除敏感词
adminApi.deleteSensitiveWord(id)
  → DELETE /api/admin/sensitive-words/:id

// 获取操作日志
adminApi.getLogs(params)
  → GET /api/admin/logs
```
