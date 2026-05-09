-- ============================================
-- 问题反馈模块
-- 创建时间: 2026-05-10
-- ============================================

USE volleyball_community;

CREATE TABLE IF NOT EXISTS `feedback` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    `user_id` BIGINT NOT NULL COMMENT '提交用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: BUG/SUGGESTION/OTHER',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/REPLIED/CLOSED',
    `reply` TEXT DEFAULT NULL COMMENT '管理员回复',
    `replied_by` BIGINT DEFAULT NULL COMMENT '回复者用户ID',
    `replied_at` DATETIME DEFAULT NULL COMMENT '回复时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_feedback_user` (`user_id`),
    INDEX `idx_feedback_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';
