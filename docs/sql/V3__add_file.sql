-- ============================================
-- 排球社区数据库变更脚本 V3 - 文件服务
-- 创建时间: 2026-04-12
-- ============================================

USE volleyball_community;

-- -------------------------------------------
-- 文件表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径（相对路径）',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型（avatar/post_image）',
    content_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';
