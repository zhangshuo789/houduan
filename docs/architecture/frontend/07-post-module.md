# 07 帖子模块

## 核心文件

| 文件 | 描述 |
|------|------|
| `Board.vue` | 板块帖子列表页 |
| `PostDetail.vue` | 帖子详情页 |
| `CreatePost.vue` | 创建/编辑帖子页 |
| `src/api/index.js` | post API封装 |

## 板块帖子列表 `Board.vue`

### 路由

```
路径: /board/:id
参数: id - 板块ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  板块名称                                │
│  板块描述                                │
├─────────────────────────────────────────┤
│  [创建帖子]  (需登录)                    │
├─────────────────────────────────────────┤
│  帖子列表 (分页)                         │
│  ┌─────────────────────────────────────┐ │
│  │ 标题                                 │ │
│  │ 作者 · 发布时间 · 点赞 · 评论         │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// Board.vue
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { board as boardApi } from '@/api'

export default {
  setup() {
    const route = useRoute()
    const board = ref(null)
    const posts = ref([])
    const page = ref(0)
    const size = 10

    const loadBoard = async () => {
      const boardId = route.params.id
      const res = await boardApi.getPosts(boardId, page.value, size)
      posts.value = res.data.content
    }

    onMounted(loadBoard)

    // 监听路由变化，重新加载
    watch(() => route.params.id, loadBoard)

    return { board, posts }
  }
}
```

## 帖子详情 `PostDetail.vue`

### 路由

```
路径: /post/:id
参数: id - 帖子ID
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  帖子标题                                │
├─────────────────────────────────────────┤
│  作者头像 | 作者昵称 | 发布时间           │
├─────────────────────────────────────────┤
│  帖子正文内容                            │
│                                          │
├─────────────────────────────────────────┤
│  [点赞] [收藏] [评论数] [举报]           │
├─────────────────────────────────────────┤
│  评论列表                               │
│  ┌─────────────────────────────────────┐ │
│  │ 评论1: 用户 · 时间 · 内容            │ │
│  │   回复1                             │ │
│  │   回复2                             │ │
│  └─────────────────────────────────────┘ │
│  ┌─────────────────────────────────────┐ │
│  │ 添加评论: [输入框] [发表]            │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// PostDetail.vue
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { post as postApi, user as userApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const toast = useToast()

    const post = ref(null)
    const comments = ref([])
    const likeStatus = ref({ isLiked: false, isFavorited: false })

    const loadPost = async () => {
      const postId = route.params.id

      // 加载帖子详情
      const postRes = await postApi.getDetail(postId)
      post.value = postRes.data

      // 加载评论
      const commentsRes = await postApi.getComments(postId, 0, 20)
      comments.value = commentsRes.data.content

      // 检查点赞状态
      if (isLoggedIn()) {
        // 获取点赞和收藏状态
        // ...
      }
    }

    const handleLike = async () => {
      const postId = post.value.id
      if (likeStatus.value.isLiked) {
        await postApi.unlike(postId)
        post.value.likeCount--
      } else {
        await postApi.like(postId)
        post.value.likeCount++
      }
      likeStatus.value.isLiked = !likeStatus.value.isLiked
    }

    const handleFavorite = async () => {
      const postId = post.value.id
      if (likeStatus.value.isFavorited) {
        await postApi.unfavorite(postId)
      } else {
        await postApi.favorite(postId)
      }
      likeStatus.value.isFavorited = !likeStatus.value.isFavorited
    }

    const handleAddComment = async (content, parentId = null) => {
      const postId = post.value.id
      await postApi.addComment(postId, content, parentId)
      toast.success('评论成功')
      // 刷新评论列表
      await loadComments()
    }

    onMounted(loadPost)

    return { post, comments, likeStatus, handleLike, handleFavorite, handleAddComment }
  }
}
```

## 创建/编辑帖子 `CreatePost.vue`

### 路由

```
创建: /create-post
编辑: /edit-post/:id
```

### 页面结构

```
┌─────────────────────────────────────────┐
│  [返回]  创建帖子 / 编辑帖子              │
├─────────────────────────────────────────┤
│  标题: [________________]               │
│                                          │
│  板块: [选择板块 ▼]                      │
│                                          │
│  内容:                                   │
│  ┌─────────────────────────────────────┐ │
│  │                                     │ │
│  │  富文本编辑器 / textarea             │ │
│  │                                     │ │
│  └─────────────────────────────────────┘ │
│                                          │
│  [发布] [保存草稿]                       │
└─────────────────────────────────────────┘
```

### 代码流转

```javascript
// CreatePost.vue
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { post as postApi, board as boardApi } from '@/api'
import { useToast } from '@/composables/useToast'

export default {
  setup() {
    const route = useRoute()
    const router = useRouter()
    const toast = useToast()

    const isEdit = computed(() => !!route.params.id)
    const form = ref({
      title: '',
      content: '',
      boardId: ''
    })
    const boards = ref([])

    onMounted(async () => {
      // 加载板块列表
      const boardsRes = await boardApi.list()
      boards.value = boardsRes.data

      // 编辑模式：加载帖子内容
      if (isEdit.value) {
        const postRes = await postApi.getDetail(route.params.id)
        form.value = {
          title: postRes.data.title,
          content: postRes.data.content,
          boardId: postRes.data.board.id
        }
      }
    })

    const handleSubmit = async () => {
      try {
        if (isEdit.value) {
          await postApi.update(route.params.id, form.value)
          toast.success('更新成功')
        } else {
          await postApi.create(form.value)
          toast.success('发布成功')
        }
        router.push(`/board/${form.value.boardId}`)
      } catch (error) {
        toast.error(error.message)
      }
    }

    return { form, boards, isEdit, handleSubmit }
  }
}
```

## API调用

```javascript
// 获取板块帖子列表
boardApi.getPosts(id, page, size)
  → GET /api/boards/:id/posts

// 获取帖子详情
postApi.getDetail(id)
  → GET /api/post/:id

// 创建帖子
postApi.create(data)
  → POST /api/post

// 更新帖子
postApi.update(id, data)
  → PUT /api/post/:id

// 删除帖子
postApi.delete(id)
  → DELETE /api/post/:id

// 点赞/取消点赞
postApi.like(id) / postApi.unlike(id)
  → POST /api/post/:id/like | DELETE /api/post/:id/unlike

// 收藏/取消收藏
postApi.favorite(id) / postApi.unfavorite(id)
  → POST /api/post/:id/favorite | DELETE /api/post/:id/unfavorite

// 获取评论
postApi.getComments(id, page, size)
  → GET /api/post/:id/comments

// 添加评论
postApi.addComment(postId, content, parentId)
  → POST /api/post/:postId/comment
```
