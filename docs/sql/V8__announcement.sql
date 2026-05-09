-- ============================================
-- 公告模块
-- 创建时间: 2026-05-10
-- ============================================

USE volleyball_community;

CREATE TABLE IF NOT EXISTS `announcement` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `published_by` BIGINT NOT NULL COMMENT '发布者用户ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_announcement_pinned_created` (`pinned` DESC, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';
