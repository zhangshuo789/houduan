# 问题反馈模块 - 前端对接文档

> 更新时间: 2026-05-10

---

## 一、反馈状态流转

```
PENDING → REPLIED → CLOSED
```

- `PENDING`: 待处理，用户刚提交
- `REPLIED`: 已回复，管理员已回复
- `CLOSED`: 已关闭

---

## 二、接口列表

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `POST /api/feedback` | POST | 用户 | 提交反馈 |
| `GET /api/feedback/mine` | GET | 用户 | 我的反馈列表 |
| `GET /api/feedback` | GET | 管理员 | 所有反馈列表 |
| `POST /api/feedback/{id}/reply` | POST | 管理员 | 回复反馈 |
| `PUT /api/feedback/{id}/close` | PUT | 管理员 | 关闭反馈 |

---

## 三、接口详情

### 3.1 提交反馈

**POST** `/api/feedback`

**Request Body:**
```json
{
  "title": "报名页面显示异常",
  "content": "在赛事报名页面点击报名按钮后一直转圈，无法完成报名。使用的是 Chrome 浏览器。",
  "category": "BUG"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | 是 | 标题，最多200字符 |
| content | String | 是 | 详细描述 |
| category | String | 是 | 分类：`BUG`(问题) / `SUGGESTION`(建议) / `OTHER`(其他) |

**Response:**
```json
{
  "code": 200,
  "message": "反馈提交成功",
  "data": {
    "id": 1,
    "userId": 5,
    "userNickname": "张三",
    "title": "报名页面显示异常",
    "content": "在赛事报名页面点击报名按钮后一直转圈...",
    "category": "BUG",
    "status": "PENDING",
    "reply": null,
    "repliedBy": null,
    "repliedAt": null,
    "createdAt": "2026-05-10T10:00:00",
    "updatedAt": "2026-05-10T10:00:00"
  }
}
```

---

### 3.2 我的反馈列表

**GET** `/api/feedback/mine?page=0&size=10`

按提交时间倒序。

**Response:**
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "报名页面显示异常",
        "content": "...",
        "category": "BUG",
        "status": "REPLIED",
        "reply": "已修复，请清除缓存后重试",
        "repliedBy": { "id": 1, "nickname": "管理员", "avatar": "..." },
        "repliedAt": "2026-05-10T12:00:00",
        "createdAt": "2026-05-10T10:00:00",
        "updatedAt": "2026-05-10T12:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 3.3 所有反馈列表（管理员）

**GET** `/api/feedback?status=PENDING&page=0&size=10`

**Query 参数：**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| status | String | 否 | 按状态筛选：PENDING / REPLIED / CLOSED，不传则返回全部 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页条数 |

**Response:** 同上，但包含所有用户的反馈。

---

### 3.4 回复反馈（管理员）

**POST** `/api/feedback/{id}/reply`

**Request Body:**
```json
{
  "reply": "已修复该问题，请清除浏览器缓存后重试。感谢反馈！"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| reply | String | 是 | 回复内容 |

回复后状态自动变为 `REPLIED`。

**Response:**
```json
{
  "code": 200,
  "message": "回复成功",
  "data": {
    "id": 1,
    "status": "REPLIED",
    "reply": "已修复该问题，请清除浏览器缓存后重试。感谢反馈！",
    "repliedBy": { "id": 1, "nickname": "管理员", "avatar": "..." },
    "repliedAt": "2026-05-10T12:00:00",
    ...
  }
}
```

---

### 3.5 关闭反馈（管理员）

**PUT** `/api/feedback/{id}/close`

**Response:**
```json
{
  "code": 200,
  "message": "反馈已关闭"
}
```

---

## 四、前端渲染建议

### 用户端 - 提交反馈页

- 表单：标题、内容（多行文本）、分类下拉选择（问题/建议/其他）
- 提交后跳转到"我的反馈"列表

### 用户端 - 我的反馈列表

- 列表项：标题、分类标签、状态标签、提交时间
- 状态标签颜色：`PENDING` 黄色、`REPLIED` 绿色、`CLOSED` 灰色
- 点击展开详情：反馈内容 + 管理员回复（如有）

### 管理端 - 反馈管理页

- 顶部 Tab 或筛选：全部 / 待处理 / 已回复 / 已关闭
- 列表项：用户昵称、标题、分类、状态、提交时间
- 点击进入详情：完整反馈内容 + 回复输入框 + 关闭按钮
- 未处理的反馈显示醒目标记（如红点）
