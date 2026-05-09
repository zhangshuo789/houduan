# 公告模块 - 前端对接文档

> 更新时间: 2026-05-10

---

## 一、接口列表

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/announcement` | GET | 所有人 | 公告列表（置顶优先） |
| `/api/announcement/{id}` | GET | 所有人 | 公告详情 |
| `/api/announcement` | POST | 管理员 | 发布公告 |
| `/api/announcement/{id}` | PUT | 管理员 | 编辑公告 |
| `/api/announcement/{id}` | DELETE | 管理员 | 删除公告 |

---

## 二、接口详情

### 2.1 公告列表

**GET** `/api/announcement?page=0&size=10`

所有人可访问，不需要登录。置顶公告始终排在最前面。

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | Integer | 否 | 页码，默认 0 |
| size | Integer | 否 | 每页条数，默认 10 |

**Response:**
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 2,
        "title": "系统维护通知",
        "content": "5月15日凌晨2点进行系统维护，预计持续2小时...",
        "pinned": true,
        "publishedBy": {
          "id": 1,
          "nickname": "管理员",
          "avatar": "https://..."
        },
        "createdAt": "2026-05-10T10:00:00",
        "updatedAt": "2026-05-10T10:00:00"
      },
      {
        "id": 1,
        "title": "欢迎加入排球社区",
        "content": "欢迎大家...",
        "pinned": false,
        "publishedBy": {
          "id": 1,
          "nickname": "管理员",
          "avatar": "https://..."
        },
        "createdAt": "2026-05-09T08:00:00",
        "updatedAt": "2026-05-09T08:00:00"
      }
    ],
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

---

### 2.2 公告详情

**GET** `/api/announcement/{id}`

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "系统维护通知",
    "content": "5月15日凌晨2点进行系统维护，预计持续2小时。维护期间所有服务暂停。",
    "pinned": true,
    "publishedBy": {
      "id": 1,
      "nickname": "管理员",
      "avatar": "https://..."
    },
    "createdAt": "2026-05-10T10:00:00",
    "updatedAt": "2026-05-10T10:00:00"
  }
}
```

---

### 2.3 发布公告（管理员）

**POST** `/api/announcement`

需要管理员登录。

**Request Body:**
```json
{
  "title": "系统维护通知",
  "content": "5月15日凌晨2点进行系统维护，预计持续2小时。",
  "pinned": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | 是 | 标题，最多200字符 |
| content | String | 是 | 公告内容 |
| pinned | Boolean | 否 | 是否置顶，默认 false |

**Response:**
```json
{
  "code": 200,
  "message": "公告发布成功",
  "data": {
    "id": 1,
    "title": "系统维护通知",
    "content": "...",
    "pinned": true,
    "publishedBy": { "id": 1, "nickname": "管理员", "avatar": "..." },
    "createdAt": "2026-05-10T10:00:00",
    "updatedAt": "2026-05-10T10:00:00"
  }
}
```

---

### 2.4 编辑公告（管理员）

**PUT** `/api/announcement/{id}`

Request Body 同发布接口，所有字段可选（传哪个更新哪个）。

```json
{
  "title": "更新后的标题",
  "pinned": false
}
```

---

### 2.5 删除公告（管理员）

**DELETE** `/api/announcement/{id}`

**Response:**
```json
{
  "code": 200,
  "message": "公告删除成功"
}
```

---

## 三、前端渲染建议

### 列表页

- 置顶公告显示置顶标记（如红色 `置顶` 标签或 📌 图标）
- 列表项展示：标题、发布时间、发布者昵称
- 内容在列表中截取摘要，点击进入详情页

### 详情页

- 展示完整标题、内容、发布时间、发布者信息
- 管理员可见编辑/删除按钮

### 管理后台

- 公告管理页面：列表 + 新增/编辑/删除操作
- 新增/编辑表单：标题输入框、内容富文本/多行文本、置顶开关
