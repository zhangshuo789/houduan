# 15 敏感词过滤模块

## 模块概述

敏感词过滤（SensitiveWordFilter）模块对用户发布的内容进行实时检测和过滤，防止违规内容传播。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── util/
│   └── SensitiveWordFilter.java          # 敏感词过滤器
├── repository/
│   └── SensitiveWordRepository.java      # 敏感词数据访问
└── entity/
    └── SensitiveWord.java                # 敏感词实体
```

## 数据表结构

**sensitive_word表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| word | VARCHAR(100) | 敏感词 |
| replacement | VARCHAR(100) | 替换词 |
| level | INT | 等级: 1=轻度 2=中度 3=重度 |

## 过滤策略

| 等级 | 策略 | 行为 |
|------|------|------|
| 1 | 替换 | 将敏感词替换为 `*` 或指定替换词 |
| 2 | 替换 | 强制替换为 `***` |
| 3 | 拒绝 | 抛出异常，拒绝发布 |

## 缓存机制

敏感词库使用5分钟本地缓存，避免频繁查询数据库：

```
敏感词库 (HashSet)
    ↑
    | 5分钟过期
    |
SensitiveWordRepository.findAll()
    ↓
加载到内存
```

## 代码流转

### 过滤内容

```
SensitiveWordFilter.filter(content)
    ↓
检查缓存是否存在敏感词库
    ↓ 缓存过期或不存在
      → SensitiveWordRepository.findAll()
      → 存入本地缓存，5分钟后过期
    ↓
遍历内容中的敏感词
    ↓
根据 level 执行:
  - level 1/2: 替换敏感词
  - level 3: 抛出 RuntimeException("内容包含违规词汇")
    ↓
return 过滤后的内容
```

### 过滤算法

使用简单的字符串替换：

```java
for (SensitiveWord word : sensitiveWords) {
    if (content.contains(word.getWord())) {
        String replacement = "*".repeat(word.getWord().length());
        content = content.replace(word.getWord(), replacement);
    }
}
```

**注意**: 实际实现可能使用DFA或AC自动机等高效算法进行大规模敏感词匹配。

## 触发时机

敏感词过滤在以下场景自动执行：

| 场景 | 调用位置 |
|------|---------|
| 发布帖子 | `PostService.createPost()` |
| 更新帖子 | `PostService.updatePost()` |
| 发布评论 | `CommentService.addComment()` |
| 发送私信 | `MessageService.sendPrivateMessage()` |
| 群组消息 | `GroupService.sendGroupMessage()` |
| 活动描述 | `EventService.createEvent()` |

## 使用示例

```java
@Autowired
private SensitiveWordFilter sensitiveWordFilter;

public Post createPost(CreatePostRequest request) {
    String filteredContent = sensitiveWordFilter.filter(request.getContent());
    // filteredContent 是已过滤的内容
    // 如果内容违规，会抛出 RuntimeException
}
```

## 管理接口（Admin）

管理员可以管理敏感词库：

| 接口 | 说明 |
|------|------|
| POST /api/admin/sensitive-word | 添加敏感词 |
| DELETE /api/admin/sensitive-word/{id} | 删除敏感词 |
| PUT /api/admin/sensitive-word/{id} | 更新敏感词/替换词 |
| GET /api/admin/sensitive-word | 获取敏感词列表 |

所有敏感词管理操作会清除缓存，确保立即生效。

## 配置

```properties
# 敏感词缓存刷新间隔（毫秒）
sensitive.word.cache.ttl=300000  # 5分钟
```
