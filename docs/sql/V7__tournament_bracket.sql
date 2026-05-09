-- ============================================
-- 赛事系统大改 - 锦标赛/淘汰赛对阵图
-- 分支: feature/tournament-bracket
-- 创建时间: 2026-05-09
-- 说明: 数据已清空，直接 DROP 旧表 + 重建
-- ============================================

USE volleyball_community;

-- -------------------------------------------
-- 1. 删除不再需要的表
-- -------------------------------------------
DROP TABLE IF EXISTS `event_subscription`;
DROP TABLE IF EXISTS `tournament_standings`;
DROP TABLE IF EXISTS `tournament_match`;
DROP TABLE IF EXISTS `event_image`;
DROP TABLE IF EXISTS `event_registration`;
DROP TABLE IF EXISTS `event`;

-- -------------------------------------------
-- 2. 重建 event 表
-- -------------------------------------------
CREATE TABLE `event` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '赛事ID',
    `title` VARCHAR(100) NOT NULL COMMENT '赛事标题',
    `description` TEXT COMMENT '赛事描述',
    `type` VARCHAR(30) NOT NULL COMMENT '类型: MATCH(比赛) / ACTIVITY(活动)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'REGISTERING' COMMENT '状态: REGISTERING/IN_PROGRESS/ENDED/CANCELLED',
    `format` VARCHAR(30) NOT NULL DEFAULT 'SINGLE_ELIMINATION' COMMENT '赛制: SINGLE_ELIMINATION / GROUP_ELIMINATION',
    `bracket_size` INT NOT NULL DEFAULT 8 COMMENT '参赛队伍数上限: 4/8/16/32',
    `current_round` INT DEFAULT NULL COMMENT '当前轮次',
    `start_time` DATETIME NOT NULL COMMENT '开赛时间（到达后自动开赛）',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `location` VARCHAR(200) NOT NULL COMMENT '比赛地点',
    `organizer` VARCHAR(100) DEFAULT NULL COMMENT '主办方',
    `requirements` TEXT COMMENT '参赛要求',
    `fee` DECIMAL(10,2) DEFAULT NULL COMMENT '报名费用',
    `contact_info` VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
    `created_by` BIGINT NOT NULL COMMENT '创建者用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_event_status` (`status`),
    INDEX `idx_event_start_time` (`start_time`),
    INDEX `idx_event_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛事/活动表';

-- -------------------------------------------
-- 3. 重建 event_registration 表
-- -------------------------------------------
CREATE TABLE `event_registration` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    `event_id` BIGINT NOT NULL COMMENT '赛事ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '报名用户ID（手动添加的队伍为NULL）',
    `team_name` VARCHAR(100) NOT NULL COMMENT '队伍名称',
    `bracket_position` INT DEFAULT NULL COMMENT 'bracket中的位置(0-based)',
    `eliminated` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已淘汰',
    `is_champion` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否冠军',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    INDEX `idx_reg_event` (`event_id`),
    INDEX `idx_reg_position` (`event_id`, `bracket_position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛事报名表';

-- -------------------------------------------
-- 4. 重建 event_image 表
-- -------------------------------------------
CREATE TABLE `event_image` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    `event_id` BIGINT NOT NULL COMMENT '赛事ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    INDEX `idx_image_event` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛事图片表';

-- -------------------------------------------
-- 5. 新建 tournament_match 表
-- -------------------------------------------
CREATE TABLE `tournament_match` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `event_id` BIGINT NOT NULL COMMENT '关联赛事ID',
    `round` INT NOT NULL COMMENT '轮次(1=首轮, 最大轮=决赛)',
    `match_order` INT NOT NULL COMMENT '该轮中的序号(从0开始)',
    `phase` VARCHAR(20) NOT NULL DEFAULT 'KNOCKOUT' COMMENT '阶段: GROUP / KNOCKOUT',
    `group_name` VARCHAR(10) DEFAULT NULL COMMENT '小组名(A/B/C..., 淘汰赛为NULL)',
    `team1_id` BIGINT DEFAULT NULL COMMENT '队伍1(event_registration.id)',
    `team2_id` BIGINT DEFAULT NULL COMMENT '队伍2(event_registration.id)',
    `winner_id` BIGINT DEFAULT NULL COMMENT '胜者(event_registration.id)',
    `score1` INT DEFAULT NULL COMMENT '队伍1比分',
    `score2` INT DEFAULT NULL COMMENT '队伍2比分',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/IN_PROGRESS/COMPLETED/BYE',
    `next_match_id` BIGINT DEFAULT NULL COMMENT '胜者晋级到的比赛ID',
    `next_match_slot` INT DEFAULT NULL COMMENT '进入下一场的槽位(1或2)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_match_event` (`event_id`),
    INDEX `idx_match_round` (`event_id`, `round`),
    INDEX `idx_match_status` (`event_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='锦标赛对阵图比赛表';

-- -------------------------------------------
-- 6. 新建 tournament_standings 表
-- -------------------------------------------
CREATE TABLE `tournament_standings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `event_id` BIGINT NOT NULL COMMENT '关联赛事ID',
    `group_name` VARCHAR(10) NOT NULL COMMENT '小组名(A/B/C...)',
    `registration_id` BIGINT NOT NULL COMMENT '队伍(event_registration.id)',
    `wins` INT NOT NULL DEFAULT 0 COMMENT '胜场',
    `losses` INT NOT NULL DEFAULT 0 COMMENT '负场',
    `points_scored` INT NOT NULL DEFAULT 0 COMMENT '总得分',
    `points_lost` INT NOT NULL DEFAULT 0 COMMENT '总失分',
    `rank` INT DEFAULT NULL COMMENT '小组内排名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_standings_event_group` (`event_id`, `group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='循环赛积分榜';
