# 08 评论模块

## 概述

评论功能集成在 `PostDetail.vue` 中，支持嵌套回复（回复的回复）。

## 评论数据结构

```json
{
  "id": 1,
  "content": "评论内容",
  "user": {
    "id": 1,
    "nickname": "用户A",
    "avatar": "http://..."
  },
  "postId": 1,
  "parentId": null,
  "createdAt": "2026-04-19T10:00:00",
  "replies": [
    {
      "id": 2,
      "content": "这是回复",
      "user": { "id": 2, "nickname": "用户B" },
      "parentId": 1,
      "replies": []
    }
  ]
}
```

## 代码流转

### 显示评论

```javascript
// PostDetail.vue
const comments = ref([])

const loadComments = async () => {
  const postId = route.params.id
  const res = await postApi.getComments(postId, 0, 50)
  comments.value = res.data.content
}

onMounted(loadComments)
```

### 添加顶级评论

```javascript
const newComment = ref('')

const handleAddComment = async () => {
  if (!newComment.value.trim()) return

  await postApi.addComment(post.value.id, newComment.value, null)
  newComment.value = ''
  await loadComments()
  toast.success('评论成功')
}
```

### 添加回复

```javascript
const replyContent = ref('')
const replyingTo = ref(null)  // 当前回复的评论ID

const handleReply = (commentId) => {
  replyingTo.value = commentId
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return

  await postApi.addComment(post.value.id, replyContent.value, replyingTo.value)
  replyContent.value = ''
  replyingTo.value = null
  await loadComments()
}
```

### 删除评论

```javascript
// src/api/index.js
export const comment = {
  delete(id) {
    return request(`/comment/${id}`, { method: 'DELETE' })
  }
}
```

## 评论UI结构

```html
<div class="comments-section">
  <!-- 评论输入框 -->
  <div class="comment-input">
    <textarea v-model="newComment" placeholder="写下你的评论..."></textarea>
    <button @click="handleAddComment">发表</button>
  </div>

  <!-- 评论列表 -->
  <div class="comment-list">
    <div v-for="comment in comments" :key="comment.id" class="comment-item">
      <div class="comment-main">
        <img :src="comment.user.avatar" class="avatar">
        <div class="comment-body">
          <div class="comment-header">
            <span class="nickname">{{ comment.user.nickname }}</span>
            <span class="time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="content">{{ comment.content }}</div>
          <div class="actions">
            <button @click="handleReply(comment.id)">回复</button>
            <button v-if="isOwner(comment.user.id)" @click="deleteComment(comment.id)">删除</button>
          </div>
        </div>
      </div>

      <!-- 嵌套回复 -->
      <div v-if="comment.replies && comment.replies.length" class="replies">
        <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
          <!-- 回复内容 -->
        </div>
      </div>

      <!-- 回复输入框 -->
      <div v-if="replyingTo === comment.id" class="reply-input">
        <textarea v-model="replyContent" :placeholder="`回复 @${comment.user.nickname}`"></textarea>
        <button @click="submitReply">发送</button>
        <button @click="replyingTo = null">取消</button>
      </div>
    </div>
  </div>
</div>
```

## API调用

```javascript
// 获取评论列表
postApi.getComments(postId, page, size)
  → GET /api/post/:postId/comments

// 添加评论
postApi.addComment(postId, content, parentId)
  → POST /api/post/:postId/comment
  // parentId = null 表示顶级评论
  // parentId = 某评论ID 表示回复该评论

// 删除评论
comment.delete(commentId)
  → DELETE /api/comment/:commentId
```
