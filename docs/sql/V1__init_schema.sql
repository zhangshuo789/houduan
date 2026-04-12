-- ============================================
-- 排球社区数据库初始化脚本 V1
-- 创建时间: 2026-04-12
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS volleyball_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE volleyball_community;

-- -------------------------------------------
-- 用户表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    bio VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 板块表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS board (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '板块ID',
    name VARCHAR(50) NOT NULL COMMENT '板块名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '板块描述',
    icon VARCHAR(50) DEFAULT NULL COMMENT '板块图标',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- -------------------------------------------
-- 帖子表
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容（支持Markdown）',
    user_id BIGINT NOT NULL COMMENT '发帖用户ID',
    board_id BIGINT NOT NULL COMMENT '所属板块ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- -------------------------------------------
-- 初始化板块数据
-- -------------------------------------------
INSERT INTO board (name, description, icon) VALUES
('技术讨论', '技战术分析、训练方法交流', '🏐'),
('赛事资讯', '国内外排球赛事报道', '🏆'),
('装备评测', '球鞋、球服、护具等装备测评', '👟'),
('约球专区', '组队约球、招募球员', '🤝');
