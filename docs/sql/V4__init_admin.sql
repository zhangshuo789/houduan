-- ============================================
-- 排球社区数据库变更脚本 V4 - 初始化管理员账号
-- 创建时间: 2026-04-18
-- ============================================

USE volleyball_community;

-- -------------------------------------------
-- 角色表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(20) NOT NULL UNIQUE COMMENT '角色名',
    description VARCHAR(100) COMMENT '角色描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- -------------------------------------------
-- 用户角色关联表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- -------------------------------------------
-- 用户状态表（用于存储禁用状态）
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS user_status (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    disabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否禁用',
    disabled_reason VARCHAR(255) COMMENT '禁用原因',
    disabled_at DATETIME COMMENT '禁用时间',
    disabled_by BIGINT COMMENT '操作人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户状态表';

-- -------------------------------------------
-- 敏感词表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    word VARCHAR(50) NOT NULL UNIQUE COMMENT '敏感词',
    replacement VARCHAR(50) DEFAULT '***' COMMENT '替换词',
    level VARCHAR(20) DEFAULT 'WARN' COMMENT '级别：WARN/BLOCK',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- -------------------------------------------
-- 内容举报表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    target_type VARCHAR(20) NOT NULL COMMENT '举报类型：POST/COMMENT/EVENT',
    target_id BIGINT NOT NULL COMMENT '被举报内容ID',
    reason TEXT NOT NULL COMMENT '举报原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/HANDLED/REJECTED',
    handled_by BIGINT COMMENT '处理人ID',
    handled_at DATETIME COMMENT '处理时间',
    handle_result VARCHAR(50) COMMENT '处理结果：CONTENT_DELETED/WARN_USER/DISMISS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报表';

-- -------------------------------------------
-- 操作日志表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS admin_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(20) COMMENT '操作对象类型',
    target_id BIGINT COMMENT '操作对象ID',
    detail TEXT COMMENT '操作详情',
    ip VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -------------------------------------------
-- 修复 user 表的 created_at 默认值（如果需要）
-- -------------------------------------------
ALTER TABLE user MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- -------------------------------------------
-- 初始化数据
-- -------------------------------------------

-- 插入 ADMIN 角色
INSERT INTO sys_role (name, description) VALUES ('ADMIN', '系统管理员');

-- 插入内置管理员账号 (密码: 123456, BCrypt加密)
-- BCrypt加密后的密码: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO user (username, password, nickname, avatar, bio, created_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '', '系统管理员', NOW())
ON DUPLICATE KEY UPDATE nickname='管理员', password=VALUES(password);

-- 给管理员分配 ADMIN 角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, sys_role r WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id=r.id;
