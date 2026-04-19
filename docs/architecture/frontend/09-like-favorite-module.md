# 09 点赞收藏模块

## 概述

点赞和收藏功能集成在 `PostDetail.vue` 中，用于用户对帖子进行互动。

## 点赞/收藏状态

```javascript
const likeStatus = ref({
  isLiked: false,      // 当前用户是否点赞
  isFavorited: false   // 当前用户是否收藏
})

const post = ref({
  likeCount: 0,        // 总点赞数
  commentCount: 0      // 总评论数
})
```

## 代码流转

### 点赞操作

```
用户点击 [点赞] 按钮
    ↓
handleLike() 执行
    ↓
likeStatus.value.isLiked === true ?
    ↓ Yes                    ↓ No
postApi.unlike(id)      postApi.like(id)
    ↓                        ↓
post.value.likeCount--  post.value.likeCount++
    ↓                        ↓
isLiked = false         isLiked = true
    ↓                        ↓
Toast: "取消点赞"         Toast: "点赞成功"
```

### 完整代码

```javascript
// PostDetail.vue
const handleLike = async () => {
  const postId = post.value.id

  try {
    if (likeStatus.value.isLiked) {
      await postApi.unlike(postId)
      post.value.likeCount--
      likeStatus.value.isLiked = false
      toast.success('已取消点赞')
    } else {
      await postApi.like(postId)
      post.value.likeCount++
      likeStatus.value.isLiked = true
      toast.success('点赞成功')
    }
  } catch (error) {
    toast.error(error.message)
  }
}

const handleFavorite = async () => {
  const postId = post.value.id

  try {
    if (likeStatus.value.isFavorited) {
      await postApi.unfavorite(postId)
      likeStatus.value.isFavorited = false
      toast.success('已取消收藏')
    } else {
      await postApi.favorite(postId)
      likeStatus.value.isFavorited = true
      toast.success('收藏成功')
    }
  } catch (error) {
    toast.error(error.message)
  }
}
```

## 点赞状态检查

```javascript
// 页面加载时检查点赞状态
onMounted(async () => {
  // 加载帖子详情...

  // 检查点赞状态
  const user = getUserFromStorage()
  if (user.value) {
    // 调用API获取当前用户对帖子的点赞/收藏状态
    const statusRes = await postApi.getLikeStatus(postId)
    likeStatus.value.isLiked = statusRes.data.isLiked

    const favRes = await postApi.getFavoriteStatus(postId)
    likeStatus.value.isFavorited = favRes.data.isFavorited
  }
})
```

## UI展示

```html
<div class="post-actions">
  <button
    :class="{ active: likeStatus.isLiked }"
    @click="handleLike"
  >
    <span v-if="likeStatus.isLiked">❤️</span>
    <span v-else>🤍</span>
    点赞 {{ post.likeCount }}
  </button>

  <button
    :class="{ active: likeStatus.isFavorited }"
    @click="handleFavorite"
  >
    <span v-if="likeStatus.isFavorited">⭐</span>
    <span v-else>☆</span>
    收藏
  </button>

  <button @click="scrollToComments">
    💬 评论 {{ post.commentCount }}
  </button>
</div>
```

## API调用

```javascript
// 点赞
postApi.like(postId)
  → POST /api/post/:postId/like

// 取消点赞
postApi.unlike(postId)
  → DELETE /api/post/:postId/unlike

// 收藏
postApi.favorite(postId)
  → POST /api/post/:postId/favorite

// 取消收藏
postApi.unfavorite(postId)
  → DELETE /api/post/:postId/unfavorite

// 获取点赞状态
postApi.getLikeStatus(postId)
  → GET /api/post/:postId/likeStatus

// 获取收藏状态
postApi.getFavoriteStatus(postId)
  → GET /api/post/:postId/favoriteStatus
```
