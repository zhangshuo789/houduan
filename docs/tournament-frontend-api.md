# 锦标赛/淘汰赛模块 - 前端对接文档

> 分支: `feature/tournament-bracket`
> 更新时间: 2026-05-09

---

## 一、赛事状态流转

```
REGISTERING → IN_PROGRESS → ENDED
     ↓
 CANCELLED
```

- `REGISTERING`: 报名中，队伍正在报名，报名即分配 bracket 位置
- `IN_PROGRESS`: 比赛进行中，组织者逐场选胜者
- `ENDED`: 冠军已产生
- `CANCELLED`: 赛事已取消

**自动流转：**
- 到达 `startTime` 时，`REGISTERING` → `IN_PROGRESS`（自动开赛，触发 bracket 生成）
- 决赛选胜者后，`IN_PROGRESS` → `ENDED`（自动结束）
- 组织者可手动提前开赛

---

## 二、赛制类型（format）

| 值 | 说明 |
|---|---|
| `SINGLE_ELIMINATION` | 单败淘汰，输一场即淘汰 |
| `GROUP_ELIMINATION` | 小组循环赛 + 淘汰赛（暂未完全实现） |

---

## 三、API 接口

### 3.1 创建赛事

**POST** `/api/event`

需要登录。

**Request Body:**
```json
{
  "title": "2026年社区排球赛",
  "description": "一年一度的社区排球比赛",
  "type": "MATCH",
  "format": "SINGLE_ELIMINATION",
  "bracketSize": 8,
  "startTime": "2026-06-01T09:00:00",
  "endTime": "2026-06-01T18:00:00",
  "location": "社区体育馆",
  "organizer": "排球协会",
  "requirements": "每队6人",
  "fee": 50.00,
  "contactInfo": "13800138000"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | String | 是 | 2-100字符 |
| description | String | 是 | 赛事描述 |
| type | String | 是 | `MATCH`(比赛) / `ACTIVITY`(活动) |
| format | String | 是 | `SINGLE_ELIMINATION` / `GROUP_ELIMINATION` |
| bracketSize | Integer | 是 | 参赛队伍数上限，最小2（如 4/8/16） |
| startTime | DateTime | 是 | 开赛时间，到达后自动开赛 |
| endTime | DateTime | 是 | 结束时间 |
| location | String | 是 | 地点 |
| organizer | String | 否 | 主办方 |
| requirements | String | 否 | 参赛要求 |
| fee | Decimal | 否 | 报名费 |
| contactInfo | String | 否 | 联系方式 |

**Response:**
```json
{
  "code": 200,
  "message": "赛事创建成功",
  "data": {
    "id": 1,
    "title": "2026年社区排球赛",
    "status": "REGISTERING",
    "format": "SINGLE_ELIMINATION",
    "bracketSize": 8,
    "currentRound": null,
    "registrationCount": 0,
    "createdBy": { "id": 1, "nickname": "张三", "avatar": "..." },
    ...
  }
}
```

---

### 3.2 赛事列表

**GET** `/api/event?page=0&size=10`

**Response:**
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "2026年社区排球赛",
        "status": "REGISTERING",
        "format": "SINGLE_ELIMINATION",
        "bracketSize": 8,
        "currentRound": null,
        "registrationCount": 3,
        "startTime": "2026-06-01T09:00:00",
        ...
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 3.3 赛事详情

**GET** `/api/event/{id}`

**Response:** 同单个赛事对象，字段同上。

---

### 3.4 报名（自动分配 bracket 位置）

**POST** `/api/event/{id}/register`

需要登录。

**Request Body:**
```json
{
  "teamName": "飞鱼队"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| teamName | String | 是 | 队伍名称 |

**成功 Response:**
```json
{
  "code": 200,
  "message": "报名成功",
  "data": {
    "id": 1,
    "eventId": 1,
    "userId": 5,
    "teamName": "飞鱼队",
    "bracketPosition": 0,
    "eliminated": false,
    "isChampion": false,
    "createdAt": "2026-05-09T22:00:00"
  }
}
```

**错误情况:**
- 已报名：`"已报名该赛事"`
- 报名已满：`"报名已满"`
- 赛事不在报名阶段：`"赛事当前不在报名阶段"`

---

### 3.5 获取对阵图（Bracket）

**GET** `/api/event/{id}/bracket`

所有人可见，不需要登录。

**Response:**
```json
{
  "code": 200,
  "data": {
    "eventId": 1,
    "format": "SINGLE_ELIMINATION",
    "bracketSize": 8,
    "registeredCount": 6,
    "eventStatus": "IN_PROGRESS",
    "rounds": [
      {
        "round": 1,
        "phase": "KNOCKOUT",
        "groupName": null,
        "matches": [
          {
            "matchId": 1,
            "matchOrder": 0,
            "team1": {
              "registrationId": 1,
              "teamName": "飞鱼队",
              "bracketPosition": 0,
              "eliminated": false,
              "isChampion": false
            },
            "team2": {
              "registrationId": 5,
              "teamName": "旋风队",
              "bracketPosition": 7,
              "eliminated": true,
              "isChampion": false
            },
            "winnerId": 1,
            "score1": 25,
            "score2": 18,
            "status": "COMPLETED"
          },
          {
            "matchId": 2,
            "matchOrder": 1,
            "team1": {
              "registrationId": 3,
              "teamName": "猛虎队",
              "bracketPosition": 1,
              "eliminated": false,
              "isChampion": false
            },
            "team2": null,
            "winnerId": null,
            "score1": null,
            "score2": null,
            "status": "PENDING"
          }
        ]
      },
      {
        "round": 2,
        "phase": "KNOCKOUT",
        "groupName": null,
        "matches": [
          {
            "matchId": 5,
            "matchOrder": 0,
            "team1": {
              "registrationId": 1,
              "teamName": "飞鱼队",
              "bracketPosition": 0,
              "eliminated": false,
              "isChampion": false
            },
            "team2": null,
            "winnerId": null,
            "status": "PENDING"
          }
        ]
      }
    ]
  }
}
```

**字段说明：**

| 字段 | 说明 |
|---|---|
| `rounds` | 按轮次排列的数组，round=1 为首轮，最后一轮为决赛 |
| `phase` | `KNOCKOUT`(淘汰赛) / `GROUP`(小组循环赛) |
| `team1` / `team2` | null 表示该位置空（等待晋级或轮空） |
| `status` | `PENDING`(未开始) / `COMPLETED`(已结束) / `BYE`(轮空) |
| `eliminated` | true = 已淘汰 |
| `isChampion` | true = 冠军 |

---

### 3.6 手动开赛（组织者/管理员）

**POST** `/api/event/{id}/bracket/start`

组织者或管理员可提前开赛，不受开赛时间限制。1支队直接标记为冠军。

**Response:**
```json
{
  "code": 200,
  "message": "赛事已开赛"
}
```

---

### 3.7 记录比赛结果（选胜者）

**PUT** `/api/event/{id}/match/{matchId}/result`

组织者或管理员操作。选完胜者后，胜者自动晋级下一轮，败者标记淘汰。

**Request Body:**
```json
{
  "winnerId": 1,
  "score1": 25,
  "score2": 18
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| winnerId | Long | 是 | 胜者 registrationId（必须是该场比赛的参赛队伍之一） |
| score1 | Integer | 否 | 队伍1比分 |
| score2 | Integer | 否 | 队伍2比分 |

**Response:**
```json
{
  "code": 200,
  "message": "比赛结果已记录"
}
```

**自动行为：**
- 败者 `eliminated` 设为 true
- 胜者写入下一轮对应 match 的 team1 或 team2
- 如果是决赛（无下一轮），胜者标记 `isChampion=true`，赛事状态变为 `ENDED`

---

### 3.8 手动添加队伍（组织者/管理员）

**POST** `/api/event/{id}/bracket/team`

位置由系统自动分配（与普通报名相同的分散策略）。

**Request Body:**
```json
{
  "teamName": "新队伍"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| teamName | String | 是 | 队伍名称 |

---

### 3.9 删除队伍（组织者/管理员）

**DELETE** `/api/event/{id}/bracket/team/{regId}`

删除队伍后，该队伍在所有 match 中的位置会被清空。

---

## 四、前端渲染对阵图建议

### 4.1 Bracket 数据结构对应 UI

```
rounds[0] (Round 1)     rounds[1] (Round 2)     rounds[2] (Final)
┌─────────────┐
│ match[0]    │
│ team1 vs team2 ──────┐
└─────────────┘        │
                       ├── match[0]
┌─────────────┐        │    team1 vs team2 ────┐
│ match[1]    │        │                       │
│ team1 vs team2 ──────┘                       ├── 冠军
└─────────────┘                                │
                                               │
┌─────────────┐                                │
│ match[2]    │                                │
│ team1 vs team2 ──────┐                       │
└─────────────┘        │                       │
                       ├── match[1]            │
┌─────────────┐        │    team1 vs team2 ────┘
│ match[3]    │        │
│ team1 vs team2 ──────┘
└─────────────┘
```

### 4.2 渲染要点

- 每个 `round` 为一列，从左到右排列
- `team` 为 null 时显示"待定"或灰色占位
- `status: BYE` 的 match 显示"轮空"标签
- `eliminated: true` 的队伍显示删除线或灰色
- `isChampion: true` 的队伍高亮显示
- `winnerId` 不为 null 时，胜者高亮，败者灰显

### 4.3 组织者操作面板

根据 `eventStatus` 和用户角色（组织者/管理员）显示操作按钮：

| 赛事状态 | 可操作内容 |
|---|---|
| `REGISTERING` | 手动开赛、添加队伍、删除队伍 |
| `IN_PROGRESS` | 选择胜者（match status 为 PENDING 且两个队伍都已分配时）、添加/删除队伍 |
| `ENDED` | 只读，展示冠军 |

**判断是否可操作胜者：**
```
match.status === "PENDING" && match.team1 !== null && match.team2 !== null
```

---

## 五、完整流程示例（8队单败淘汰）

1. **创建赛事** → `POST /api/event` (bracketSize=8)
2. **队伍报名** → `POST /api/event/1/register` × 8次
   - 每次返回分配的 `bracketPosition` (0, 7, 1, 6, 2, 5, 3, 4)
3. **查看对阵图** → `GET /api/event/1/bracket`
   - 8队分配到4场首轮比赛
4. **开赛**（自动或手动）→ bracket 全部 match 记录生成
5. **第1轮** → 选4场胜者
   - `PUT /api/event/1/match/1/result` (winnerId=队伍A)
   - 胜者自动进入半决赛，败者标记淘汰
6. **半决赛** → 选2场胜者
7. **决赛** → 选胜者 → 自动标记冠军，赛事 ENDED

---

## 六、错误码

| HTTP | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 |
| 403 | 无权限（非组织者/管理员操作需权限的接口） |
| 500 | 业务异常（如"报名已满"、"赛事当前不在报名阶段"等） |
