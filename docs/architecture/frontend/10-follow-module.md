# 10 关注模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `UserProfile.vue` | 用户主页（含关注功能） |
| `src/api/index.js` | follow API封装 |

## 代码流转

### 关注/取消关注

```javascript
// UserProfile.vue
const isFollowing = ref(false)

const handleFollow = async () => {
  const userId = userInfo.value.id

  try {
    if (isFollowing.value) {
      await followApi.unfollow(userId)
      isFollowing.value = false
      stats.value.followerCount--
      toast.success('已取消关注')
    } else {
      await followApi.follow(userId)
      isFollowing.value = true
      stats.value.followerCount++
      toast.success('关注成功')
    }
  } catch (error) {
    toast.error(error.message)
  }
}
```

### 检查关注状态

```javascript
const checkFollowStatus = async () => {
  const userId = route.params.id
  const currentUser = getUserFromStorage()

  // 不能关注自己
  if (!currentUser.value || currentUser.value.id === Number(userId)) {
    return
  }

  const res = await followApi.getFollowStatus(userId)
  isFollowing.value = res.data.isFollowing
}
```

### 关注列表和粉丝列表

```javascript
// 在UserProfile.vue中
const followingList = ref([])
const followersList = ref([])

const loadFollowing = async () => {
  const res = await followApi.getFollowing(userId, 0, 20)
  followingList.value = res.data.content
}

const loadFollowers = async () => {
  const res = await followApi.getFollowers(userId, 0, 20)
  followersList.value = res.data.content
}
```

### 好友列表（双向关注）

```javascript
const friendsList = ref([])

const loadFriends = async () => {
  const res = await followApi.getFriends(userId, 0, 20)
  friendsList.value = res.data.content
}
```

## 关注按钮UI

```html
<div class="user-actions">
  <button
    v-if="currentUser && currentUser.id !== userInfo.id"
    :class="{ following: isFollowing }"
    @click="handleFollow"
  >
    {{ isFollowing ? '已关注' : '关注' }}
  </button>

  <button @click="$router.push(`/chat/${userInfo.id}`)">
    发消息
  </button>
</div>
```

## API调用

```javascript
// 关注用户
followApi.follow(userId)
  → POST /api/follow/:userId

// 取消关注
followApi.unfollow(userId)
  → DELETE /api/follow/:userId

// 获取关注状态
followApi.getFollowStatus(userId)
  → GET /api/follow/:userId/status
  → 返回: { isFollowing, isFollowed }

// 获取关注列表
followApi.getFollowing(userId, page, size)
  → GET /api/user/:userId/following

// 获取粉丝列表
followApi.getFollowers(userId, page, size)
  → GET /api/user/:userId/followers

// 获取好友列表（双向关注）
followApi.getFriends(userId, page, size)
  → GET /api/user/:userId/friends
```

## 响应数据结构

### 关注状态

```json
{
  "isFollowing": true,   // 我是否关注了对方
  "isFollowed": false    // 对方是否关注了我
}
```

### 关注/粉丝列表

```json
{
  "content": [
    {
      "id": 1,
      "username": "user1",
      "nickname": "排球达人",
      "avatar": "http://..."
    }
  ],
  "totalElements": 100,
  "totalPages": 5
}
```
