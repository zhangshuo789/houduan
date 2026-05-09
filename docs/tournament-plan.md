# 赛事系统大改 - 锦标赛/淘汰赛对阵图

## Context

当前赛事系统的报名流程太简单（只有队伍名+审核），不支持正规赛事的对阵图、晋级管理等功能。需要大改为支持锦标赛制：报名即分配bracket位置，组织者管理晋级，自动生成对阵图。

分支：`feature/tournament-bracket`

## 一、设计概览

### 赛制

- `SINGLE_ELIMINATION`：单败淘汰
- `GROUP_ELIMINATION`：小组循环 + 淘汰赛

### 核心流程

```
创建赛事(指定bracket大小+赛制+开赛时间)
→ 开放报名 → 队伍报名(立即分配bracket位置)
→ [startTime到达 或 组织者手动开赛] → IN_PROGRESS
→ 组织者逐场选胜者 → 自动晋级/淘汰
→ 决赛胜者 = 冠军 → ENDED
```

### 关键规则

- 对阵图全员可见，仅组织者+管理员可操作
- 组织者随时可增删队伍、选胜者
- 1支队直接开赛 = 冠军
- 奇数队伍 → 轮空(BYE)自动晋级
- bracket大小创建时指定（4/8/16...）

## 二、数据库设计

### 2.1 `event` 表新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| format | VARCHAR | SINGLE_ELIMINATION / GROUP_ELIMINATION |
| bracket_size | INT | 4/8/16/32 |
| current_round | INT | 当前轮次 |

### 2.2 新建 `tournament_match` 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| event_id | BIGINT | 关联赛事 |
| round | INT | 轮次（1=首轮，最大轮=决赛） |
| match_order | INT | 该轮中的序号（从0开始） |
| phase | VARCHAR | GROUP / KNOCKOUT |
| group_name | VARCHAR | 小组名（如 "A"/"B"，淘汰赛=null） |
| team1_id | BIGINT | 队伍1（event_registration.id） |
| team2_id | BIGINT | 队伍2 |
| winner_id | BIGINT | 胜者 registrationId |
| score1 | INT | 队伍1比分（可选） |
| score2 | INT | 队伍2比分（可选） |
| status | VARCHAR | PENDING / IN_PROGRESS / COMPLETED / BYE |
| next_match_id | BIGINT | 胜者晋级到哪场比赛 |
| next_match_slot | INT | 进入下一场的 1号位/2号位 |
| created_at | DATETIME | 创建时间 |

### 2.3 新建 `tournament_standings` 表（循环赛用）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| event_id | BIGINT | 关联赛事 |
| group_name | VARCHAR | 小组名 |
| registration_id | BIGINT | 队伍 |
| wins | INT | 胜场 |
| losses | INT | 负场 |
| points_scored | INT | 总得分 |
| points_lost | INT | 总失分 |
| rank | INT | 小组内排名 |

### 2.4 `event_registration` 表新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| bracket_position | INT | 在bracket中的位置（0-based） |
| eliminated | BOOLEAN | 是否已淘汰 |
| is_champion | BOOLEAN | 是否冠军 |

## 三、API 设计

### 3.1 赛事管理

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/event` | POST | 登录用户 | 创建赛事（新增format, bracketSize） |
| `/api/event` | GET | 所有人 | 赛事列表 |
| `/api/event/{id}` | GET | 所有人 | 赛事详情 |
| `/api/event/{id}` | PUT | 组织者 | 更新赛事 |
| `/api/event/{id}` | DELETE | 组织者 | 删除赛事 |

### 3.2 报名

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/event/{id}/register` | POST | 登录用户 | 报名（自动分配bracket位置） |
| `/api/event/{id}/registration` | GET | 组织者 | 查看报名列表 |

### 3.3 对阵图（TournamentController）

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/event/{id}/bracket` | GET | 所有人 | 获取完整对阵图 |
| `/api/event/{id}/bracket/start` | POST | 组织者/管理员 | 手动开赛 |
| `/api/event/{id}/match/{matchId}/result` | PUT | 组织者/管理员 | 记录比赛结果（选胜者） |
| `/api/event/{id}/bracket/team` | POST | 组织者/管理员 | 手动添加队伍到空位 |
| `/api/event/{id}/bracket/team/{regId}` | DELETE | 组织者/管理员 | 删除队伍 |
| `/api/event/{id}/standings` | GET | 所有人 | 循环赛积分榜 |

## 四、核心逻辑

### 4.1 首轮配对策略（分散配对）

8队bracket，配对关系：[0,7] [1,6] [2,5] [3,4]

分配顺序（尽量分散到不同对）：
- 第1队 → 位置0
- 第2队 → 位置7（与第1队同对，但对中第2个）
- 第3队 → 位置1（新对）
- 第4队 → 位置6（与第3队同对）
- 第5队 → 位置2（新对）
- 第6队 → 位置5
- 第7队 → 位置3
- 第8队 → 位置4

### 4.2 开赛时生成 Bracket

单败淘汰：
- bracketSize 位，log2(bracketSize) 轮
- 预创建所有 Match 记录，设置 nextMatchId/nextMatchSlot 关系
- 空位标记 BYE，BYE 对手自动晋级

循环赛+淘汰赛：
- 系统自动分组（如 8队→2组×4）
- 创建小组循环赛 Match 记录
- 淘汰赛 Match 等小组出线后再生成

### 4.3 选择胜者流程

```
selectWinner(eventId, matchId, winnerId, score1?, score2?):
  1. 校验组织者/管理员权限
  2. 更新 match: winnerId, score, status=COMPLETED
  3. loser 标记 eliminated=true
  4. nextMatch 根据 slot 写入 winnerId
  5. 如果是决赛：winner 标记 isChampion=true，event.status=ENDED
```

### 4.4 奇数队伍轮空

- 开赛时检测：如果某 Match 的 team2 为 null（未满员）
- 该 Match 标记为 BYE，team1 自动晋级到 nextMatch

### 4.5 1支队直接冠军

- 组织者手动开赛时，如果只有1支队
- 直接标记 isChampion=true，event.status=ENDED

## 五、实施步骤

### Step 1: 清除旧事件代码，建立新 Entity + Repository
- 删除旧的 EventService、EventRegistrationService、EventSubscriptionService
- 删除旧的 EventController、EventRegistrationController、EventImageController
- 删除旧的 AdminEventService 中赛事相关方法
- 保留 Event entity，在此基础上新增字段
- 保留 EventRegistration entity，新增字段
- 新建 TournamentMatch entity + repository
- 新建 TournamentStandings entity + repository

### Step 2: DTO 层
- 更新 EventRequest / EventResponse
- 新建 BracketResponse、MatchResultRequest、TournamentTeamRequest

### Step 3: TournamentService（核心业务逻辑）
- 位置分配算法
- Bracket 生成
- 选择胜者 + 自动晋级
- 删除/添加队伍
- 循环赛分组（后续扩展）

### Step 4: EventService + EventRegistrationService（重写）
- 赛事 CRUD
- 报名逻辑（含位置分配）

### Step 5: Controller 层
- 重写 EventController
- 新建 TournamentController

### Step 6: Scheduler 改造
- 自动开赛逻辑
- 移除旧状态切换

### Step 7: 编译验证 + 测试
