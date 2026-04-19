# 11 SSE实时通知模块

## 模块概述

SSE（Server-Sent Events）模块实现服务端到客户端的实时推送，用于消息提醒、群组通知、活动更新等场景。

## 核心技术

| 技术 | 说明 |
|------|------|
| Spring SseEmitter | Spring SSE实现 |
| 异步处理 | @Async 异步发送 |
| 线程安全 | ConcurrentHashMap 存储连接 |

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── SseController.java               # SSE连接接口
└── service/
    └── SseService.java                   # SSE业务逻辑
```

## 代码流转

### 建立SSE连接

```
GET /api/sse/connect?userId={userId}
Authorization: Bearer <token>
    ↓
SseController.connect(userId)
    ↓
SseService.connect(userId)
    ↓
new SseEmitter(0L) 创建永久连接的Emitter
    ↓
SseEmitter.onTimeout() 连接超时处理
SseEmitter.onCompletion() 连接关闭处理
    ↓
并存入 ConcurrentHashMap<Long, SseEmitter> emitters
    ↓
return ResponseEntity.ok().body(emitter)
    ↓
前端建立 EventSource 连接
```

### 发送消息给指定用户

```
SseService.sendMessageToUser(userId, eventType, data)
    ↓
SseEmitter emitter = emitters.get(userId)
    ↓ emitter == null → 用户未在线，跳过
    ↓
try {
  emitter.send(SseEventBuilder
    .event(eventType)
    .data(data))
} catch (IOException e) {
  emitters.remove(userId)
  emitter.complete()
}
```

### 广播消息给所有在线用户

```
SseService.broadcast(eventType, data)
    ↓
emitters.forEach((userId, emitter) -> {
  try {
    emitter.send(...)
  } catch (...) {
    emitters.remove(userId)
  }
})
```

### 移除连接

```
SseService.removeEmitter(userId)
    ↓
emitters.remove(userId)
```

## 事件类型

| 事件名 | 触发场景 | data内容 |
|--------|---------|---------|
| `message` | 收到新私信 | Message对象 |
| `group_message` | 群组新消息 | Message对象 |
| `follow` | 有人关注 | User对象 |
| `like` | 帖子被点赞 | { postId, likeCount } |
| `comment` | 帖子被评论 | Comment对象 |
| `event_update` | 活动更新 | Event对象 |
| `registration` | 活动报名 | Registration对象 |

## 前端接入示例

```javascript
const userId = getCurrentUserId();
const eventSource = new EventSource(`/api/sse/connect?userId=${userId}`);

eventSource.addEventListener('message', (event) => {
  const data = JSON.parse(event.data);
  console.log('收到私信:', data);
});

eventSource.addEventListener('like', (event) => {
  const data = JSON.parse(event.data);
  updatePostLikeCount(data.postId, data.likeCount);
});

eventSource.onerror = () => {
  console.log('SSE连接断开，5秒后重连');
  setTimeout(() => location.reload(), 5000);
};
```

## 与消息模块的集成

```
用户A发送私信给用户B
    ↓
MessageService.sendPrivateMessage()
    ↓
SseService.sendMessageToUser(userB, "message", message)
    ↓
用户B的前端实时收到私信通知
```

## 与活动模块的集成

```
用户报名活动成功
    ↓
EventRegistrationService.register()
    ↓
SseService.sendMessageToUser(organizerId, "registration", registration)
    ↓
活动组织者实时收到报名通知
```

## 配置

SSE连接超时设置为0表示永久连接（通过心跳维持）。如有需要，可在application.properties中配置心跳间隔。

```properties
# SSE心跳间隔（可选）
sse.heartbeat.interval=30000
```
