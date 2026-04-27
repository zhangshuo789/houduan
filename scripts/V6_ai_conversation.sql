-- AI 对话功能数据库变更
-- 执行时间: 2026-04-27
-- 包含: AI会话表、AI消息表

-- =====================================================
-- 表1: ai_conversation (AI会话表)
-- =====================================================
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

-- =====================================================
-- 表2: ai_message (AI消息表)
-- =====================================================
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system',
    `content` TEXT COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
    `thinking` TEXT COLLATE utf8mb4_unicode_ci COMMENT 'AI思考过程',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- =====================================================
-- 外键约束 (如果需要严格的外键关联)
-- =====================================================
-- ALTER TABLE `ai_message` ADD CONSTRAINT `fk_message_conversation`
--     FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation`(`id`) ON DELETE CASCADE;

-- =====================================================
-- 说明
-- =====================================================
-- 1. ai_conversation: 存储用户的 AI 对话会话
--    - 每个用户可以有多个会话
--    - title 会自动更新为用户第一条消息的前20字符
--
-- 2. ai_message: 存储会话中的每条消息
--    - role: user(用户消息) / assistant(AI回复)
--    - content: 消息正文内容
--    - thinking: AI 思考过程(仅 thinking=true 时有值)
--
-- 3. 表已配置 JPA 实体自动创建,此文件仅供人工执行或回滚使用