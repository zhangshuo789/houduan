# 阶段四：赛事日历功能设计

> 本文档定义赛事日历功能的完整设计方案

---

## 1. 概述

赛事日历功能支持两类内容：
- **比赛**：正式比赛、联赛等，需管理员发布
- **活动**：约球、训练营、友谊赛等，任何登录用户可发布

用户可以订阅赛事获取更新推送，也可以报名参加（需审核）。

---

## 2. 数据模型

### 2.1 Event 赛事表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| title | VARCHAR(100) | 赛事标题 |
| description | TEXT | 赛事描述 |
| type | VARCHAR(20) | 赛事类型：MATCH(比赛)/ACTIVITY(活动) |
| status | VARCHAR(20) | 状态：PREPARING(筹备中)/REGISTERING(报名中)/IN_PROGRESS(已开始)/ENDED(已结束)/CANCELLED(已取消) |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| location | VARCHAR(255) | 地点 |
| organizer | VARCHAR(100) | 主办方 |
| requirements | TEXT | 参赛要求 |
| max_participants | INT | 最大参赛队伍数 |
| fee | DECIMAL(10,2) | 费用 |
| contact_info | VARCHAR(255) | 联系方式 |
| registration_deadline | DATETIME | 报名截止时间 |
| created_by | BIGINT (FK) | 发布者用户ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.2 EventImage 赛事图片表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| event_id | BIGINT (FK) | 关联赛事ID |
| image_url | VARCHAR(500) | 图片URL |
| sort_order | INT | 排序顺序 |

### 2.3 EventSubscription 赛事订阅表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| event_id | BIGINT (FK) | 关联赛事ID |
| user_id | BIGINT (FK) | 订阅用户ID |
| created_at | DATETIME | 订阅时间 |

**约束**：event_id + user_id 唯一

### 2.4 EventRegistration 赛事报名表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 主键 |
| event_id | BIGINT (FK) | 关联赛事ID |
| user_id | BIGINT (FK) | 报名用户ID |
| team_name | VARCHAR(100) | 球队名称 |
| contact_person | VARCHAR(50) | 联系人 |
| contact_phone | VARCHAR(20) | 联系方式 |
| team_size | INT | 参赛人数 |
| status | VARCHAR(20) | 状态：PENDING(待审核)/APPROVED(已通过)/REJECTED(已拒绝) |
| reviewed_by | BIGINT | 审核人用户ID |
| reviewed_at | DATETIME | 审核时间 |
| created_at | DATETIME | 报名时间 |

---

## 3. API 接口设计

### 3.1 赛事列表

```
GET /api/event
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码（从0开始） |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "2026春季排球联赛",
        "description": "...",
        "type": "MATCH",
        "status": "REGISTERING",
        "startTime": "2026-05-01T09:00:00",
        "endTime": "2026-05-03T18:00:00",
        "location": "国家体育馆",
        "organizer": "排球协会",
        "requirements": "身体健康，18岁以上",
        "maxParticipants": 16,
        "fee": 500.00,
        "contactInfo": "电话：xxx",
        "registrationDeadline": "2026-04-25T23:59:59",
        "imageUrls": ["http://xxx.com/1.jpg"],
        "createdBy": {
          "id": 1,
          "nickname": "管理员",
          "avatar": "http://xxx.com/avatar.png"
        },
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

**说明**：按 startTime 升序排列（即按赛事开始时间排序）

---

### 3.2 赛事详情

```
GET /api/event/{id}
```

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "2026春季排球联赛",
    "description": "...",
    "type": "MATCH",
    "status": "REGISTERING",
    "startTime": "2026-05-01T09:00:00",
    "endTime": "2026-05-03T18:00:00",
    "location": "国家体育馆",
    "organizer": "排球协会",
    "requirements": "身体健康，18岁以上",
    "maxParticipants": 16,
    "fee": 500.00,
    "contactInfo": "电话：xxx",
    "registrationDeadline": "2026-04-25T23:59:59",
    "imageUrls": ["http://xxx.com/1.jpg", "http://xxx.com/2.jpg"],
    "createdBy": {
      "id": 1,
      "nickname": "管理员",
      "avatar": "http://xxx.com/avatar.png"
    },
    "createdAt": "2026-04-01T10:00:00",
    "updatedAt": "2026-04-01T10:00:00",
    "registrationCount": 8,
    "subscriberCount": 50,
    "isSubscribed": true,
    "hasRegistered": false
  }
}
```

**说明**：`isSubscribed` 和 `hasRegistered` 字段：已登录返回 true/false，未登录返回 null

---

### 3.3 创建赛事

```
POST /api/event
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 赛事标题，5-100字符 |
| description | string | 是 | 赛事描述 |
| type | string | 是 | MATCH(比赛) 或 ACTIVITY(活动) |
| startTime | datetime | 是 | 开始时间 |
| endTime | datetime | 是 | 结束时间 |
| location | string | 是 | 地点 |
| organizer | string | 否 | 主办方 |
| requirements | string | 否 | 参赛要求 |
| maxParticipants | int | 否 | 最大参赛队伍数 |
| fee | decimal | 否 | 费用 |
| contactInfo | string | 否 | 联系方式 |
| registrationDeadline | datetime | 否 | 报名截止时间 |
| imageUrls | array | 否 | 图片URL列表 |

**返回数据**：

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "title": "...",
    ...
  }
}
```

**权限**：
- type=ACTIVITY：任何登录用户可发布
- type=MATCH：仅管理员可发布

---

### 3.4 更新赛事

```
PUT /api/event/{id}
```

**权限**：仅管理员或发布者可更新

---

### 3.5 删除赛事

```
DELETE /api/event/{id}
```

**权限**：仅管理员可删除

---

### 3.6 订阅赛事

```
POST /api/event/{id}/subscribe
```

**返回数据**：

```json
{
  "code": 200,
  "message": "订阅成功",
  "data": null
}
```

---

### 3.7 取消订阅

```
DELETE /api/event/{id}/subscribe
```

---

### 3.8 报名赛事

```
POST /api/event/{id}/register
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| teamName | string | 是 | 球队名称 |
| contactPerson | string | 是 | 联系人 |
| contactPhone | string | 是 | 联系方式 |
| teamSize | int | 是 | 参赛人数 |

**返回数据**：

```json
{
  "code": 200,
  "message": "报名成功，等待审核",
  "data": {
    "id": 1,
    "teamName": "冠军队",
    "contactPerson": "张三",
    "contactPhone": "13800138000",
    "teamSize": 12,
    "status": "PENDING",
    "createdAt": "2026-04-01T10:00:00"
  }
}
```

---

### 3.9 查看报名列表

```
GET /api/event/{id}/registration
```

**权限**：仅管理员或发布者可查看

**返回数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "teamName": "冠军队",
        "contactPerson": "张三",
        "contactPhone": "138****8000",
        "teamSize": 12,
        "status": "PENDING",
        "createdAt": "2026-04-01T10:00:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### 3.10 审核报名

```
PUT /api/event/{id}/registration/{regId}
```

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | boolean | 是 | true=通过，false=拒绝 |

**返回数据**：

```json
{
  "code": 200,
  "message": "审核成功",
  "data": null
}
```

**SSE推送**：审核结果会通过 SSE 推送给报名用户（事件名：registrationResult）

---

### 3.11 我的订阅列表

```
GET /api/user/{id}/subscriptions
```

**查询参数**：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | int | 否 | 0 | 页码 |
| size | int | 否 | 10 | 每页数量 |

**返回数据**：同赛事列表格式

---

## 4. SSE 实时推送

### 4.1 建立连接

```
GET /api/sse/connect?token={jwtToken}
```

### 4.2 赛事相关事件

| 事件名 | 触发时机 | data 内容 |
|--------|----------|-----------|
| eventUpdate | 赛事信息更新 | Event 对象 |
| eventStatusChanged | 赛事状态变更 | {eventId, oldStatus, newStatus} |
| newRegistration | 新报名（通知主办方） | Registration 对象 |
| registrationResult | 报名审核结果（通知报名者） | {eventId, eventTitle, approved, reason} |

---

## 5. 项目结构

新增文件：

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── entity/
│   ├── Event.java              # 赛事实体
│   ├── EventImage.java         # 赛事图片实体
│   ├── EventSubscription.java  # 赛事订阅实体
│   └── EventRegistration.java  # 赛事报名实体
├── repository/
│   ├── EventRepository.java
│   ├── EventImageRepository.java
│   ├── EventSubscriptionRepository.java
│   └── EventRegistrationRepository.java
├── service/
│   ├── EventService.java
│   ├── EventSubscriptionService.java
│   └── EventRegistrationService.java
├── controller/
│   ├── EventController.java
│   └── EventRegistrationController.java
├── dto/
│   ├── request/
│   │   ├── EventRequest.java
│   │   └── EventRegistrationRequest.java
│   └── response/
│       ├── EventResponse.java
│       ├── EventListResponse.java
│       └── EventRegistrationResponse.java
└── enums/
    └── EventType.java          # MATCH, ACTIVITY
    └── EventStatus.java        # PREPARING, REGISTERING, IN_PROGRESS, ENDED, CANCELLED
    └── RegistrationStatus.java # PENDING, APPROVED, REJECTED
```

---

## 6. 数据库表 SQL

```sql
CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL COMMENT 'MATCH:比赛, ACTIVITY:活动',
    status VARCHAR(20) NOT NULL DEFAULT 'PREPARING' COMMENT 'PREPARING:筹备中, REGISTERING:报名中, IN_PROGRESS:已开始, ENDED:已结束, CANCELLED:已取消',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    organizer VARCHAR(100),
    requirements TEXT,
    max_participants INT,
    fee DECIMAL(10,2) DEFAULT 0,
    contact_info VARCHAR(255),
    registration_deadline DATETIME,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

CREATE TABLE event_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    INDEX idx_event_id (event_id)
);

CREATE TABLE event_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_event_user (event_id, user_id),
    INDEX idx_event_id (event_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE event_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    team_size INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING:待审核, APPROVED:已通过, REJECTED:已拒绝',
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_event_id (event_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);
```

---

## 7. 安全配置更新

SecurityConfig 新增接口权限：

| 接口 | 权限要求 |
|------|----------|
| GET /api/event | 公开 |
| GET /api/event/{id} | 公开 |
| POST /api/event | 管理员/活动类型人人可发 |
| PUT /api/event/{id} | 管理员或发布者 |
| DELETE /api/event/{id} | 管理员 |
| POST /api/event/{id}/subscribe | 登录 |
| DELETE /api/event/{id}/subscribe | 登录 |
| POST /api/event/{id}/register | 登录 |
| GET /api/event/{id}/registration | 管理员或发布者 |
| PUT /api/event/{id}/registration/{regId} | 管理员或发布者 |
| GET /api/user/{id}/subscriptions | 登录（本人） |
