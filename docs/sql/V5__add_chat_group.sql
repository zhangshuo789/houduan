-- V5: 新增群组表，重命名群成员表
-- 执行时间: 2026-04-19

-- 1. 创建群组表 chat_group
CREATE TABLE IF NOT EXISTS `chat_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '群组ID',
    `name` VARCHAR(50) NOT NULL COMMENT '群名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '群描述',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '群头像（文件ID）',
    `owner_id` BIGINT NOT NULL COMMENT '群主ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- 2. 重命名群成员表 group_member -> chat_group_member
RENAME TABLE `group_member` TO `chat_group_member`;

-- 3. 添加索引到 chat_group_member
ALTER TABLE `chat_group_member`
    ADD INDEX `idx_group_id` (`group_id`),
    ADD INDEX `idx_user_id` (`user_id`);

-- 4. 数据迁移：将现有群组数据从 message 表迁移到 chat_group 表
-- 注意：原有群聊是通过在 message 表插入一条 type='group' 的消息来创建的
--       message.id 作为 group_id 使用
--       message.content 存储群名称

-- 迁移现有群组数据（假设 message 表中已存在使用这种方式的群组）
-- 此迁移脚本需要根据实际数据情况执行，以下为示例

-- 示例：迁移一个现有群组（假设 message.id = 1 是群组消息）
-- INSERT INTO `chat_group` (`id`, `name`, `owner_id`, `created_at`)
-- SELECT m.id, m.content, m.sender_id, m.created_at
-- FROM `message` m
-- WHERE m.type = 'group' AND m.target_id = 0;

-- 5. 如果需要删除旧的占位消息（type='group' 且 target_id=0 的消息）
-- DELETE FROM `message` WHERE `type` = 'group' AND `target_id` = 0;

-- 6. 添加字段注释（如果数据库驱动支持）
-- ALTER TABLE `chat_group_member` COMMENT '群成员表';
