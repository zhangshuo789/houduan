# 03 板块模块

## 模块概述

板块（Board）是帖子的分类容器，用户发帖时需要选择所属板块。板块是公开的，任何用户都可以浏览板块下的帖子列表。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── BoardController.java              # 板块接口
├── service/
│   └── BoardService.java                 # 板块业务逻辑
├── repository/
│   └── BoardRepository.java              # 板块数据访问
└── entity/
    └── Board.java                        # 板块实体
```

## 数据表结构

**board表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 板块名称 |
| description | TEXT | 板块描述 |
| icon | VARCHAR(255) | 板块图标URL |
| created_at | DATETIME | 创建时间 |

## 代码流转

### 获取所有板块

```
GET /api/boards
    ↓
BoardService.getAllBoards()
    ↓
BoardRepository.findAll()
    ↓
return 板块列表
```

### 获取板块下的帖子

```
GET /api/boards/{boardId}/posts?page=0&size=10
    ↓
BoardService.getBoardPosts(boardId, pageable)
    ↓
BoardRepository.existsById(boardId) 检查板块存在
    ↓ 不存在 → 抛出RuntimeException("板块不存在")
    ↓
PostRepository.findByBoardIdOrderByCreatedAtDesc(boardId, pageable)
    ↓
PostService.enrichPostResponses() 补充点赞数、评论数等信息
    ↓
return 分页帖子列表
```

## 接口详情

### GET /api/boards 获取所有板块

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "技术交流",
      "description": "分享排球技术心得",
      "icon": "https://example.com/board1.png"
    }
  ]
}
```

### GET /api/boards/{boardId}/posts 获取板块帖子

**查询参数**:
- `page`: 页码，默认0
- `size`: 每页数量，默认10

**响应**: 分页的帖子列表
