-- ============================================
-- 排球社区数据库变更脚本 V2 - 互动功能
-- 创建时间: 2026-04-12
-- ============================================

USE volleyball_community;

-- -------------------------------------------
-- 评论表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    post_id BIGINT NOT NULL COMMENT '被评论的帖子ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（为空表示一级评论）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- -------------------------------------------
-- 点赞表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `like` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    post_id BIGINT NOT NULL COMMENT '被点赞的帖子ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- -------------------------------------------
-- 收藏表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '收藏用户ID',
    post_id BIGINT NOT NULL COMMENT '被收藏的帖子ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_post (user_id, post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';
