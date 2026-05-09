-- ============================================
-- 赛事订阅表
-- 创建时间: 2026-05-10
-- ============================================

USE volleyball_community;

CREATE TABLE IF NOT EXISTS `event_subscription` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `event_id` BIGINT NOT NULL COMMENT '赛事ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    UNIQUE KEY `uk_event_user` (`event_id`, `user_id`),
    INDEX `idx_sub_event` (`event_id`),
    INDEX `idx_sub_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='赛事订阅表';
