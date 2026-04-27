# AI 对话功能对接指南

> 本文档为前端开发人员提供 AI 对话功能的接口对接说明

---

## 功能概述

AI 对话模块基于 DeepSeek API 实现，支持：
- 多会话管理：用户可创建多个独立对话
- 上下文记忆：每次对话保留完整聊天历史
- 流式响应：支持 SSE 实时推送 AI 回复
- AI 思考过程：可选开启 AI 推理过程展示

---

## 前置要求

1. 用户需已登录获取 JWT Token
2. 后端已配置 DeepSeek API Key

---

## 接口列表

| 方法 | 路径 | 说明 | 优先级 |
|------|------|------|--------|
| POST | `/api/ai/conversations` | 创建新会话 | P0 |
| GET | `/api/ai/conversations` | 获取会话列表 | P0 |
| DELETE | `/api/ai/conversations/{id}` | 删除会话 | P1 |
| GET | `/api/ai/conversations/{id}/messages` | 获取历史消息 | P0 |
| POST | `/api/ai/conversations/{id}/messages` | 发送消息（非流式） | P0 |
| POST | `/api/ai/conversations/{id}/messages/stream` | 发送消息（流式） | P1 |

---

## 接口详情

### 1. 创建会话

```
POST /api/ai/conversations
```

**请求**：无需请求体

**响应**：
```json
{
  "code": 200,
  "message": "会话创建成功",
  "data": {
    "id": 1,
    "title": "新对话",
    "createdAt": "2026-04-27T10:00:00",
    "updatedAt": "2026-04-27T10:00:00"
  }
}
```

---

### 2. 获取会话列表

```
GET /api/ai/conversations
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "如何提高扣球高度？",
      "createdAt": "2026-04-27T10:00:00",
      "updatedAt": "2026-04-27T10:05:00"
    }
  ]
}
```

**说明**：
- 按更新时间倒序排列
- 首次消息后，title 会自动更新为用户消息的前 20 字符

---

### 3. 删除会话

```
DELETE /api/ai/conversations/{id}
```

**响应**：
```json
{
  "code": 200,
  "message": "会话已删除",
  "data": null
}
```

---

### 4. 获取历史消息

```
GET /api/ai/conversations/{id}/messages
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "role": "user",
      "content": "如何提高扣球高度？",
      "thinking": null,
      "createdAt": "2026-04-27T10:00:00"
    },
    {
      "id": 2,
      "role": "assistant",
      "content": "提高扣球高度需要从力量、弹跳、技术等方面入手...",
      "thinking": null,
      "createdAt": "2026-04-27T10:00:05"
    }
  ]
}
```

**字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| role | string | `user`=用户, `assistant`=AI |
| content | string | 消息内容 |
| thinking | string | AI 思考过程（仅 `thinking=true` 时有值） |

---

### 5. 发送消息（非流式）

```
POST /api/ai/conversations/{id}/messages
```

**请求**：
```json
{
  "content": "如何提高扣球高度？",
  "thinking": false,
  "stream": false
}
```

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| content | string | 是 | | 消息内容 |
| thinking | boolean | 否 | false | 是否启用 AI 思考过程 |
| stream | boolean | 否 | false | 当前请传 false |

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "role": "assistant",
    "content": "以下是提高扣球高度的建议...",
    "thinking": null,
    "createdAt": "2026-04-27T10:00:10"
  }
}
```

---

### 6. 发送消息（流式/SSE）⭐ 推荐

```
POST /api/ai/conversations/{id}/messages/stream
```

**请求**：
```json
{
  "content": "如何提高扣球高度？",
  "thinking": false,
  "stream": true
}
```

**响应**：SSE 事件流（`text/event-stream`）

**SSE 事件格式**：
```
event: message
data: 你好

event: message
data: ，我

event: message
data: 是

event: done
data:
```

**前端实现示例**：

```javascript
// 使用 fetch + ReadableStream 实现 SSE
async function sendMessageStream(conversationId, content, onChunk, onDone) {
    const response = await fetch(`/api/ai/conversations/${conversationId}/messages/stream`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getJwtToken()}`
        },
        body: JSON.stringify({ content, stream: true, thinking: false })
    });

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value);
        const lines = chunk.split('\n');

        for (const line of lines) {
            if (line.startsWith('event: message')) {
                // 找到对应的 data
            }
            if (line.startsWith('data: ')) {
                const data = line.substring(6);
                if (data) {
                    onChunk(data);
                }
            }
        }
    }

    onDone();
}

// 使用示例
let fullContent = '';
sendMessageStream(1, '你好', (chunk) => {
    fullContent += chunk;
    console.log('收到片段:', chunk);
}, () => {
    console.log('回复完成:', fullContent);
});
```

---

## 页面布局建议

### AI 对话页面结构

```
┌─────────────────────────────────────┐
│  AI 助手                            │
├─────────────────────────────────────┤
│  会话列表          │  聊天区域      │
│  ┌─────────────┐   │  ┌───────────┐ │
│  │ 会话1      │   │  │ 消息1     │ │
│  │ 会话2      │   │  │ 消息2     │ │
│  │ + 新建会话  │   │  └───────────┘ │
│  └─────────────┘   │  ┌───────────┐ │
│                    │  │ 输入框...  │ │
│                    │  └───────────┘ │
└─────────────────────────────────────┘
```

### 交互流程

1. 进入页面 → 调用 `GET /api/ai/conversations` 获取会话列表
2. 点击会话 → 调用 `GET /api/ai/conversations/{id}/messages` 获取历史
3. 发送消息 → 调用 `POST .../messages/stream` 流式接收回复
4. 新建会话 → 调用 `POST /api/ai/conversations`
5. 删除会话 → 调用 `DELETE /api/ai/conversations/{id}`

---

## 配置说明

AI 功能配置在 `application.properties` 中：

```properties
ai.deepseek.api-key=${DEEPSEEK_API_KEY:}        # API Key
ai.deepseek.base-url=${DEEPSEEK_BASE_URL:https://api.deepseek.com}
ai.deepseek.model=${DEEPSEEK_MODEL:deepseek-v4-flash}  # 模型
ai.deepseek.thinking-enabled=false              # 默认关闭思考过程
```

---

## 注意事项

1. **流式接口注意**：SSE 流式接口需要在请求体中传入 `stream: true`，响应 Content-Type 为 `text/event-stream`

2. **thinking 参数**：开启后会返回 AI 的推理过程，可能影响响应速度和内容长度

3. **敏感词过滤**：AI 回复内容已通过敏感词过滤

4. **消息保存**：每次发送消息都会自动保存到数据库，包含完整的上下文

---

## 错误处理

| code | 说明 | 处理方式 |
|------|------|----------|
| 200 | 成功 | 正常处理 |
| 400 | 参数错误 | 检查请求参数 |
| 401 | 未登录 | 跳转登录页 |
| 403 | 无权限 | 检查会话所属 |
| 404 | 会话不存在 | 刷新会话列表 |
| 500 | 服务器错误 | 提示用户重试 |

---

## 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2026-04-27 | v1.0 | 首次发布 AI 对话功能 |