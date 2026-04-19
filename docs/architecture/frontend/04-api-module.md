# 04 API接口模块

## 核心文件

**`src/api/index.js`**: 统一封装所有后端API

## API模块索引

| 模块 | 前缀 | 说明 |
|------|------|------|
| `auth` | `/auth` | 认证相关 |
| `user` | `/user` | 用户相关 |
| `board` | `/boards` | 板块相关 |
| `post` | `/post` | 帖子相关 |
| `comment` | `/comment` | 评论相关 |
| `file` | `/file` | 文件相关 |
| `follow` | `/follow` | 关注相关 |
| `message` | `/message` | 私信相关 |
| `group` | `/group` | 群组相关 |
| `event` | `/event` | 活动相关 |
| `admin` | `/admin` | 管理相关 |
| `report` | `/admin/reports` | 举报相关 |

## 请求封装

```javascript
// src/api/index.js

const request = async (endpoint, options = {}) => {
  const headers = getHeaders()

  const res = await fetch(`${apiBaseUrl}${apiPrefix}${endpoint}`, {
    ...options,
    headers: {
      ...headers,
      ...options.headers
    }
  })

  const json = await res.json()

  if (json.code !== 200) {
    throw new Error(json.message || '请求失败')
  }

  return json
}
```

## API详细列表

### Auth认证模块

```javascript
export const auth = {
  register(data) {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  login(data) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  }
}
```

### User用户模块

```javascript
export const user = {
  getInfo(id) { return request(`/user/${id}`) },
  update(id, data) {
    return request(`/user/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  },
  getFavorites(id, page, size) {
    return request(`/user/${id}/favorites?page=${page}&size=${size}`)
  },
  getStats(id) { return request(`/user/${id}/stats`) },
  getFeed(id, page, size) {
    return request(`/user/${id}/feed?page=${page}&size=${size}`)
  },
  getPosts(id, page, size) {
    return request(`/user/${id}/posts?page=${page}&size=${size}`)
  }
}
```

### Post帖子模块

```javascript
export const post = {
  create(data) {
    return request('/post', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  getDetail(id) { return request(`/post/${id}`) },
  update(id, data) {
    return request(`/post/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  },
  delete(id) {
    return request(`/post/${id}`, { method: 'DELETE' })
  },
  like(id) {
    return request(`/post/${id}/like`, { method: 'POST' })
  },
  unlike(id) {
    return request(`/post/${id}/unlike`, { method: 'DELETE' })
  },
  favorite(id) {
    return request(`/post/${id}/favorite`, { method: 'POST' })
  },
  unfavorite(id) {
    return request(`/post/${id}/unfavorite`, { method: 'DELETE' })
  },
  getComments(id, page, size) {
    return request(`/post/${id}/comments?page=${page}&size=${size}`)
  },
  addComment(postId, content, parentId) {
    return request(`/post/${postId}/comment`, {
      method: 'POST',
      body: JSON.stringify({ content, parentId })
    })
  }
}
```

### Message消息模块

```javascript
export const message = {
  getConversations() { return request('/message/conversations') },
  getPrivateMessages(userId, page, size) {
    return request(`/message/private/${userId}?page=${page}&size=${size}`)
  },
  sendPrivateMessage(userId, content) {
    return request(`/message/private/${userId}`, {
      method: 'POST',
      body: JSON.stringify({ content })
    })
  },
  markAsRead(conversationWithUserId) {
    return request('/message/read', {
      method: 'POST',
      body: JSON.stringify({ conversationWithUserId })
    })
  },
  getUnreadCount() { return request('/message/unread-count') }
}
```

### Group群组模块

```javascript
export const group = {
  create(data) {
    return request('/group', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  getInfo(id) { return request(`/group/${id}`) },
  getMembers(id) { return request(`/group/${id}/members`) },
  addMember(groupId, userId) {
    return request(`/group/${groupId}/members?userId=${userId}`, {
      method: 'POST'
    })
  },
  removeMember(groupId, userId) {
    return request(`/group/${groupId}/members/${userId}`, {
      method: 'DELETE'
    })
  },
  sendMessage(groupId, content) {
    return request(`/group/${groupId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content })
    })
  },
  getMessages(groupId, page, size) {
    return request(`/group/${groupId}/messages?page=${page}&size=${size}`)
  }
}
```

### Event活动模块

```javascript
export const event = {
  list(page, size) {
    return request(`/event?page=${page}&size=${size}`)
  },
  getDetail(id) { return request(`/event/${id}`) },
  create(data) {
    return request('/event', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  subscribe(id) {
    return request(`/event/${id}/subscribe`, { method: 'POST' })
  },
  unsubscribe(id) {
    return request(`/event/${id}/subscribe`, { method: 'DELETE' })
  },
  register(id, data) {
    return request(`/event/${id}/register`, {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  getRegistrations(id) {
    return request(`/event/${id}/registration`)
  }
}
```

## 代码流转

### API调用流程

```
组件调用: post.getDetail(1)
    ↓
API函数内部: request('/post/1')
    ↓
request() 添加认证头
    ↓
fetch 发送请求
    ↓
解析响应 JSON
    ↓
code === 200 → 返回 { data }
    ↓
code !== 200 → throw Error → useToast显示错误
```

### 文件上传流程

```javascript
// 文件上传特殊处理
export const file = {
  upload(formData) {
    return fetch(`${apiBaseUrl}/file/upload`, {
      method: 'POST',
      headers: {
        'Authorization': getToken()
        // 不设置 Content-Type，让fetch自动处理 multipart
      },
      body: formData
    }).then(res => res.json())
  }
}
```

## Composable封装

**`src/composables/useApi.js`**:

```javascript
import { useToast } from './useToast'

export function useApi(apiFn) {
  const toast = useToast()

  return async (...args) => {
    try {
      const result = await apiFn(...args)
      return result
    } catch (error) {
      toast.error(error.message || '操作失败')
      throw error
    }
  }
}
```

使用示例：
```javascript
const fetchPost = useApi(postApi.getDetail)
const data = await fetchPost(1)
```
