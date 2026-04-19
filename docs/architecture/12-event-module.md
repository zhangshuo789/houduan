# 12 活动模块

## 模块概述

活动（Event）模块用于组织排球社区的活动/比赛，支持活动发布、报名管理、取消等功能。

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   ├── EventController.java              # 活动接口
│   └── EventRegistrationController.java   # 报名接口
├── service/
│   ├── EventService.java                  # 活动业务逻辑
│   └── EventRegistrationService.java      # 报名业务逻辑
├── repository/
│   ├── EventRepository.java               # 活动数据访问
│   ├── EventImageRepository.java          # 活动图片访问
│   ├── EventSubscriptionRepository.java   # 订阅数据访问
│   └── EventRegistrationRepository.java   # 报名数据访问
└── entity/
    ├── Event.java                        # 活动实体
    ├── EventImage.java                    # 活动图片
    ├── EventSubscription.java             # 活动订阅
    └── EventRegistration.java            # 活动报名
```

## 数据表结构

**event表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(100) | 活动标题 |
| description | TEXT | 活动描述 |
| type | VARCHAR(50) | 活动类型 |
| status | VARCHAR(20) | 状态: PUBLISHED/CANCELLED/ENDED |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| location | VARCHAR(200) | 地点 |
| organizer_id | BIGINT | 组织者ID |
| fee | DECIMAL | 费用 |
| max_participants | INT | 最大参与人数 |
| created_at | DATETIME | 创建时间 |

**event_registration表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| event_id | BIGINT | 活动ID |
| user_id | BIGINT | 报名用户ID |
| team_name | VARCHAR(100) | 队伍名称（可选） |
| contact_person | VARCHAR(50) | 联系人 |
| contact_phone | VARCHAR(20) | 联系电话 |
| team_size | INT | 队伍人数 |
| status | VARCHAR(20) | 状态: PENDING/APPROVED/REJECTED |
| reviewed_by | BIGINT | 审核人ID |
| created_at | DATETIME | 报名时间 |

## 代码流转

### 发布活动

```
POST /api/event
Authorization: Bearer <token>
    ↓
[EventRequest] title, description, type, startTime, endTime, location, fee, maxParticipants
    ↓
EventService.createEvent(organizerId, request)
    ↓
敏感词过滤 description
    ↓
new Event(...)
    ↓
EventRepository.save(event)
    ↓
return EventResponse
```

### 获取活动列表

```
GET /api/event?page=0&size=10&status=PUBLISHED
    ↓
EventService.getEventList(status, pageable)
    ↓
EventRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
    ↓
return Page<EventResponse>
```

### 获取活动详情

```
GET /api/event/{eventId}
    ↓
EventService.getEventById(eventId)
    ↓
EventRepository.findById(eventId)
    ↓
EventRegistrationRepository.countByEventIdAndStatus(eventId, APPROVED) 已通过报名数
    ↓
return EventDetailResponse { event, registrationCount, isOrganizer, ... }
```

### 取消活动

```
PUT /api/event/{eventId}/cancel
Authorization: Bearer <token>
    ↓
EventService.cancelEvent(eventId, userId)
    ↓
EventRepository.findById(eventId)
    ↓ 非组织者 → 抛出RuntimeException("无权限")
    ↓
event.status = CANCELLED
    ↓
EventRepository.save(event)
    ↓
SseService通知所有报名者活动已取消
    ↓
return ApiResponse.success("活动已取消")
```

### 订阅活动（关注）

```
POST /api/event/{eventId}/subscribe
Authorization: Bearer <token>
    ↓
EventService.subscribeEvent(eventId, userId)
    ↓
EventSubscriptionRepository.existsByEventIdAndUserId 检查是否已订阅
    ↓
new EventSubscription(eventId, userId)
    ↓
return ApiResponse.success("订阅成功")
```

### 报名参加活动

```
POST /api/event/{eventId}/register
Authorization: Bearer <token>
    ↓
[RegistrationRequest] teamName, contactPerson, contactPhone, teamSize
    ↓
EventRegistrationService.register(eventId, userId, request)
    ↓
EventRepository.findById(eventId)
    ↓
EventRegistrationRepository.countByEventIdAndStatus(eventId, APPROVED) >= maxParticipants
    ↓ 满员 → 抛出RuntimeException("活动已满员")
    ↓
检查是否重复报名
    ↓
new EventRegistration(APPROVED或PENDING)
    ↓
EventRegistrationRepository.save(registration)
    ↓
SseService.sendMessageToUser(organizerId, "registration", registration) 通知组织者
    ↓
return ApiResponse.success("报名成功")
```

### 审核报名

```
PUT /api/event/{eventId}/registration/{regId}
Authorization: Bearer <token>
    ↓
EventRegistrationService.approveRegistration(eventId, regId, adminId, approved)
    ↓
EventRegistrationRepository.findById(regId)
    ↓
更新 status = APPROVED/REJECTED, reviewedBy = adminId
    ↓
SseService通知报名者审核结果
    ↓
return ApiResponse.success("审核完成")
```

## 接口详情

### POST /api/event 发布活动

**请求体**:
```json
{
  "title": "string",
  "description": "string",
  "type": "MATCH/TRAINING/ACTIVITY",
  "startTime": "2026-05-01T09:00:00",
  "endTime": "2026-05-01T17:00:00",
  "location": "体育馆",
  "fee": 0,
  "maxParticipants": 20
}
```

### GET /api/event/{eventId}/registration 获取活动报名列表

**响应**: 分页的EventRegistration列表（仅组织者可见完整信息）
