/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : volleyball_community

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 18/05/2026 00:40:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `admin_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_log
-- ----------------------------

-- ----------------------------
-- Table structure for admin_notification
-- ----------------------------
DROP TABLE IF EXISTS `admin_notification`;
CREATE TABLE `admin_notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_read` bit(1) NOT NULL,
  `sent_at` datetime(6) NOT NULL,
  `target_user_id` bigint NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_notification
-- ----------------------------
INSERT INTO `admin_notification` VALUES (1, '2222', b'1', '2026-04-19 16:08:45.367339', NULL, '222', 'BROADCAST');
INSERT INTO `admin_notification` VALUES (2, '222', b'1', '2026-04-19 16:08:55.756837', NULL, '22', 'BROADCAST');
INSERT INTO `admin_notification` VALUES (3, '2222', b'1', '2026-04-19 16:10:36.543178', NULL, '2222', 'BROADCAST');

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_conversation
-- ----------------------------
INSERT INTO `ai_conversation` VALUES (1, '2026-04-27 23:24:21.009042', '你是什么模型啊', '2026-04-27 23:38:39.393946', 3);

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `conversation_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `thinking` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ai_message
-- ----------------------------
INSERT INTO `ai_message` VALUES (1, '你是什么模型', 1, '2026-04-27 23:24:28.659053', 'user', NULL);
INSERT INTO `ai_message` VALUES (2, '你是什么模型', 1, '2026-04-27 23:35:26.740302', 'user', NULL);
INSERT INTO `ai_message` VALUES (3, '你是什么模型啊', 1, '2026-04-27 23:38:37.177179', 'user', NULL);
INSERT INTO `ai_message` VALUES (4, '我是DeepSeek，由深度求索公司创造的AI助手！我是最新版本的DeepSeek模型，具备强大的自然语言理解和生成能力，支持处理长文本、文件上传、联网搜索（需手动开启）等功能。如果你对我的技术细节或版本号有疑问，建议查阅官方文档和公告获取最新信息。有什么可以帮你的吗？😊', 1, '2026-04-27 23:38:39.375501', 'assistant', NULL);
INSERT INTO `ai_message` VALUES (5, '哇塞，那你一定很厉害吧', 1, '2026-04-27 23:40:33.392341', 'user', NULL);
INSERT INTO `ai_message` VALUES (6, '哈哈，谢谢夸奖！😊 其实厉害不厉害，还是要看能不能帮到你～ 我可以处理很多任务，比如回答问题、写文章、翻译、分析数据，甚至陪你聊天解闷。不过最重要的是——**你用得顺手，那才叫真厉害**！有什么想试试的功能吗？我随时待命！🚀', 1, '2026-04-27 23:40:37.088660', 'assistant', NULL);
INSERT INTO `ai_message` VALUES (7, '我叫李昊，认识一下', 1, '2026-04-27 23:49:36.712683', 'user', NULL);
INSERT INTO `ai_message` VALUES (8, '哈喽李昊！很高兴认识你！😊 我是DeepSeek，一个热爱解决问题和聊天的AI助手。无论是学习、工作还是日常趣事，我都很乐意陪你一起探索。有什么想聊的、想问的，随时开口——我叫你“昊哥”还是“小李”？随你喜欢！👋', 1, '2026-04-27 23:49:38.489037', 'assistant', NULL);

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `pinned` bit(1) NOT NULL,
  `published_by` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (1, 'test公告', '2026-05-10 00:35:14.155691', b'1', 3, 'test', '2026-05-10 01:13:17.625822');
INSERT INTO `announcement` VALUES (2, '置顶测试', '2026-05-10 01:12:32.657767', b'0', 3, '你好', '2026-05-10 01:12:32.657767');

-- ----------------------------
-- Table structure for board
-- ----------------------------
DROP TABLE IF EXISTS `board`;
CREATE TABLE `board`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '板块ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '板块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '板块描述',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '板块图标',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '板块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of board
-- ----------------------------
INSERT INTO `board` VALUES (1, '技术讨论', '技战术分析、训练方法交流', '🏐', '2026-04-12 13:26:49');
INSERT INTO `board` VALUES (2, '赛事资讯', '国内外排球赛事报道', '🏆', '2026-04-12 13:26:49');
INSERT INTO `board` VALUES (3, '装备评测', '球鞋、球服、护具等装备测评', '👟', '2026-04-12 13:26:49');
INSERT INTO `board` VALUES (4, '约球专区', '组队约球、招募球员', '🤝', '2026-04-12 13:26:49');
INSERT INTO `board` VALUES (5, '我是test', 'test板块', NULL, '2026-05-10 01:28:49');

-- ----------------------------
-- Table structure for chat_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_group`;
CREATE TABLE `chat_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `owner_id` bigint NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_group
-- ----------------------------
INSERT INTO `chat_group` VALUES (1, NULL, '2026-04-27 21:50:52.905169', '123456', 'test', 3, '2026-04-27 21:50:52.905169');

-- ----------------------------
-- Table structure for chat_group_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_member`;
CREATE TABLE `chat_group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `banned` bit(1) NOT NULL,
  `group_id` bigint NOT NULL,
  `joined_at` datetime(6) NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKl5gsb646b5hdvy2scwwoqtmdi`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_group_member
-- ----------------------------
INSERT INTO `chat_group_member` VALUES (1, b'0', 1, '2026-04-27 21:50:52.925314', 'OWNER', 3);
INSERT INTO `chat_group_member` VALUES (2, b'0', 1, '2026-04-29 21:53:04.220829', 'MEMBER', 2);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (4, '嘿嘿', '2026-04-12 21:31:43.136631', NULL, 1, 1);
INSERT INTO `comment` VALUES (6, '你好', '2026-04-14 22:37:43.339359', 4, 1, 1);
INSERT INTO `comment` VALUES (7, '你好呀', '2026-04-14 22:57:26.490510', 6, 1, 1);
INSERT INTO `comment` VALUES (8, '你好', '2026-04-15 22:08:56.048727', 7, 1, 2);
INSERT INTO `comment` VALUES (9, '我也来试一试\n', '2026-04-18 22:41:54.994092', NULL, 1, 2);
INSERT INTO `comment` VALUES (10, '这个评论做的真不错', '2026-04-18 22:42:20.388339', NULL, 1, 2);
INSERT INTO `comment` VALUES (11, '哈哈哈', '2026-04-19 16:28:07.861833', 10, 1, 3);

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '赛事ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `bracket_size` int NOT NULL DEFAULT 8 COMMENT '参赛队伍数上限: 4/8/16/32',
  `current_round` int NULL DEFAULT NULL COMMENT '当前轮次',
  `start_time` datetime NOT NULL COMMENT '开赛时间（到达后自动开赛）',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `organizer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `requirements` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '报名费用',
  `contact_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_by` bigint NOT NULL COMMENT '创建者用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_status`(`status` ASC) USING BTREE,
  INDEX `idx_event_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_event_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赛事/活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event
-- ----------------------------
INSERT INTO `event` VALUES (1, '202排球春季赛', '春季赛', 'MATCH', 'IN_PROGRESS', 'SINGLE_ELIMINATION', 16, 1, '2026-05-12 00:21:00', '2026-05-13 23:21:00', '社区体育馆', '排球协会', NULL, 20.00, '19817115293', 3, '2026-05-09 23:22:10', '2026-05-09 23:55:25');

-- ----------------------------
-- Table structure for event_image
-- ----------------------------
DROP TABLE IF EXISTS `event_image`;
CREATE TABLE `event_image`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_image_event`(`event_id` ASC) USING BTREE,
  CONSTRAINT `FK9oirj7cwmu7k91vr0m13hqh8b` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赛事图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event_image
-- ----------------------------

-- ----------------------------
-- Table structure for event_registration
-- ----------------------------
DROP TABLE IF EXISTS `event_registration`;
CREATE TABLE `event_registration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `event_id` bigint NOT NULL COMMENT '赛事ID',
  `user_id` bigint NULL DEFAULT NULL,
  `team_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `bracket_position` int NULL DEFAULT NULL COMMENT 'bracket中的位置(0-based)',
  `eliminated` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已淘汰',
  `is_champion` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否冠军',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reg_event`(`event_id` ASC) USING BTREE,
  INDEX `idx_reg_position`(`event_id` ASC, `bracket_position` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赛事报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event_registration
-- ----------------------------
INSERT INTO `event_registration` VALUES (1, 1, 3, '我是测试队伍', 0, 1, 0, '2026-05-09 23:22:26');
INSERT INTO `event_registration` VALUES (6, 1, NULL, 'test1', 15, 1, 0, '2026-05-09 23:34:03');
INSERT INTO `event_registration` VALUES (7, 1, NULL, 'test2', 1, 1, 0, '2026-05-09 23:54:35');
INSERT INTO `event_registration` VALUES (8, 1, NULL, 'test4', 14, 0, 0, '2026-05-09 23:54:45');
INSERT INTO `event_registration` VALUES (9, 1, NULL, 'test66', 2, 1, 0, '2026-05-09 23:54:52');
INSERT INTO `event_registration` VALUES (10, 1, NULL, 'teat77', 13, 0, 0, '2026-05-09 23:54:55');
INSERT INTO `event_registration` VALUES (11, 1, NULL, 'waefawef', 3, 0, 0, '2026-05-09 23:54:57');
INSERT INTO `event_registration` VALUES (12, 1, NULL, 'fdsfasfdas', 12, 1, 0, '2026-05-09 23:54:59');
INSERT INTO `event_registration` VALUES (13, 1, NULL, 'fdasfasdfasdf', 4, 0, 0, '2026-05-09 23:55:01');
INSERT INTO `event_registration` VALUES (14, 1, NULL, 'wwawEDw', 11, 0, 0, '2026-05-09 23:55:03');
INSERT INTO `event_registration` VALUES (15, 1, NULL, 'tyr5ewrtw', 5, 1, 0, '2026-05-09 23:55:06');
INSERT INTO `event_registration` VALUES (16, 1, NULL, 'qweqwas', 10, 1, 0, '2026-05-09 23:55:08');
INSERT INTO `event_registration` VALUES (17, 1, NULL, 'ewqasagfd', 6, 1, 0, '2026-05-09 23:55:10');
INSERT INTO `event_registration` VALUES (18, 1, NULL, 'yttyy', 9, 1, 0, '2026-05-09 23:55:12');
INSERT INTO `event_registration` VALUES (19, 1, NULL, 'dafad', 7, 1, 0, '2026-05-09 23:55:14');
INSERT INTO `event_registration` VALUES (20, 1, NULL, 'eaw', 8, 0, 0, '2026-05-09 23:55:16');

-- ----------------------------
-- Table structure for event_subscription
-- ----------------------------
DROP TABLE IF EXISTS `event_subscription`;
CREATE TABLE `event_subscription`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKpjhm223dy8r2vdenduge1ktlp`(`event_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of event_subscription
-- ----------------------------

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr1039wocmhfvha4rw8xgqrny9`(`user_id` ASC, `post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, '2026-04-12 21:05:35.485600', 1, 1);
INSERT INTO `favorite` VALUES (2, '2026-04-18 22:42:28.335906', 1, 2);

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `replied_at` datetime(6) NULL DEFAULT NULL,
  `replied_by` bigint NULL DEFAULT NULL,
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback
-- ----------------------------
INSERT INTO `feedback` VALUES (1, 'BUG', '修复bug', '2026-05-10 00:49:40.965185', '2026-05-10 00:50:54.893411', 3, '修复完毕', 'REPLIED', 'bug', '2026-05-10 00:50:54.902635', 3);

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file
-- ----------------------------
INSERT INTO `file` VALUES (1, 'image/jpeg', '2026-04-12 16:12:51.680276', 'avator.jpg', 'avatars/user_1/335b35b5-204c-49b5-ad7c-b17a74d93223.jpg', 79507, 'avatar', '335b35b5-204c-49b5-ad7c-b17a74d93223.jpg', 1);
INSERT INTO `file` VALUES (2, 'image/jpeg', '2026-04-15 22:08:28.287847', 'logo.jpg', 'avatars/user_2/aed7751d-74a2-4c16-a07a-2a7b8d77a72f.jpg', 26482, 'avatar', 'aed7751d-74a2-4c16-a07a-2a7b8d77a72f.jpg', 2);
INSERT INTO `file` VALUES (3, 'image/jpeg', '2026-04-18 23:53:48.781178', 'lihao.jpg', 'avatars/user_3/af036a6f-7061-4432-bde4-0da402848dd2.jpg', 105696, 'avatar', 'af036a6f-7061-4432-bde4-0da402848dd2.jpg', 3);
INSERT INTO `file` VALUES (4, 'image/jpeg', '2026-04-19 13:21:48.706257', 'lihao.jpg', 'post_image/f8a57ccb-dddb-4bbb-9a1b-570de068c023.jpg', 105696, 'post_image', 'f8a57ccb-dddb-4bbb-9a1b-570de068c023.jpg', 2);
INSERT INTO `file` VALUES (5, 'image/jpeg', '2026-04-19 13:21:48.714415', '前面手持.jpg', 'post_image/d7a60b6e-72ab-488d-a6d0-970dd88af907.jpg', 478832, 'post_image', 'd7a60b6e-72ab-488d-a6d0-970dd88af907.jpg', 2);
INSERT INTO `file` VALUES (6, 'image/jpeg', '2026-04-19 13:21:48.723447', '前面.jpg', 'post_image/188eea31-0094-4a54-a11c-a2ae36929d4f.jpg', 226633, 'post_image', '188eea31-0094-4a54-a11c-a2ae36929d4f.jpg', 2);
INSERT INTO `file` VALUES (7, 'image/jpeg', '2026-04-19 13:21:48.733021', '背面手持.jpg', 'post_image/06acc66c-109b-4d12-a6fe-f37f2cbe3a01.jpg', 496728, 'post_image', '06acc66c-109b-4d12-a6fe-f37f2cbe3a01.jpg', 2);
INSERT INTO `file` VALUES (8, 'image/jpeg', '2026-04-19 13:42:27.901370', 'avator.jpg', 'post_image/e14730ec-3b15-4bd3-b205-fe8189aa345b.jpg', 79507, 'post_image', 'e14730ec-3b15-4bd3-b205-fe8189aa345b.jpg', 3);
INSERT INTO `file` VALUES (9, 'image/png', '2026-04-19 14:07:16.477172', '系统架构.png', 'post_image/007eff4a-e88a-4844-95dd-c677c380b556.png', 48620, 'post_image', '007eff4a-e88a-4844-95dd-c677c380b556.png', 3);

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `followee_id` bigint NOT NULL,
  `follower_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKrsb4np9gwp6pkj8w9vj06x6hs`(`follower_id` ASC, `followee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follow
-- ----------------------------
INSERT INTO `follow` VALUES (1, '2026-04-15 22:09:04.597126', 1, 2);
INSERT INTO `follow` VALUES (2, '2026-04-29 21:52:48.990434', 3, 2);

-- ----------------------------
-- Table structure for friendship
-- ----------------------------
DROP TABLE IF EXISTS `friendship`;
CREATE TABLE `friendship`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `friend_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKcwc2ibrki5nln3q1lhkr19xi4`(`user_id` ASC, `friend_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of friendship
-- ----------------------------

-- ----------------------------
-- Table structure for group_member
-- ----------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `banned` bit(1) NOT NULL,
  `group_id` bigint NOT NULL,
  `joined_at` datetime(6) NOT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK72t0d8wy6qkbgxj89o9prokdo`(`group_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_member
-- ----------------------------

-- ----------------------------
-- Table structure for like
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKaki4jm8p46k911htet9cfta2l`(`user_id` ASC, `post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like
-- ----------------------------
INSERT INTO `like` VALUES (1, '2026-04-12 21:05:32.378804', 1, 1);
INSERT INTO `like` VALUES (2, '2026-04-18 22:42:29.137611', 1, 2);

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `sender_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, '你好', '2026-04-16 21:57:36.004897', 2, 1, 'private');
INSERT INTO `message` VALUES (2, '你好', '2026-04-16 21:58:08.051525', 2, 1, 'private');
INSERT INTO `message` VALUES (3, '嘿嘿，还记得我吗', '2026-04-18 10:45:31.653320', 2, 1, 'private');
INSERT INTO `message` VALUES (4, 'nihao', '2026-04-18 14:23:10.987813', 2, 1, 'private');
INSERT INTO `message` VALUES (5, 'nihao', '2026-04-18 14:23:23.896716', 1, 2, 'private');
INSERT INTO `message` VALUES (6, '我是李昊', '2026-04-18 20:19:13.435995', 2, 1, 'private');
INSERT INTO `message` VALUES (7, '哇塞，我也是李昊', '2026-04-18 20:22:10.003353', 1, 2, 'private');
INSERT INTO `message` VALUES (8, '牛逼', '2026-04-18 20:22:26.438374', 1, 2, 'private');
INSERT INTO `message` VALUES (9, '通讯成功了属于是', '2026-04-18 20:22:36.150831', 1, 2, 'private');
INSERT INTO `message` VALUES (10, '在测试一下', '2026-04-18 20:22:41.359223', 1, 2, 'private');
INSERT INTO `message` VALUES (11, '接着测试', '2026-04-18 20:22:47.198575', 1, 2, 'private');
INSERT INTO `message` VALUES (12, '接着测试一次', '2026-04-18 20:22:50.863799', 1, 2, 'private');
INSERT INTO `message` VALUES (13, '再来一次把', '2026-04-18 22:43:17.825641', 1, 2, 'private');
INSERT INTO `message` VALUES (14, '你好呀', '2026-04-18 22:43:20.852941', 1, 2, 'private');
INSERT INTO `message` VALUES (15, '我是管理员', '2026-04-19 14:30:36.948855', 3, 1, 'private');
INSERT INTO `message` VALUES (16, '你犯错了', '2026-04-19 14:31:47.369479', 3, 1, 'private');
INSERT INTO `message` VALUES (17, '非常nice', '2026-04-19 14:32:04.374015', 1, 2, 'private');
INSERT INTO `message` VALUES (18, '我感觉这种真挺好的', '2026-04-19 14:32:09.135341', 1, 2, 'private');
INSERT INTO `message` VALUES (19, '真的假的', '2026-04-19 14:32:17.517933', 1, 3, 'private');
INSERT INTO `message` VALUES (20, '真的呀', '2026-04-19 14:32:25.733508', 1, 3, 'private');
INSERT INTO `message` VALUES (21, '你以为呢', '2026-04-19 14:32:28.714793', 1, 3, 'private');
INSERT INTO `message` VALUES (22, '嘿嘿', '2026-04-19 14:32:30.378866', 1, 3, 'private');
INSERT INTO `message` VALUES (24, '竟然没有人', '2026-04-27 21:51:14.223923', 3, 1, 'group');
INSERT INTO `message` VALUES (25, '有没有人啊', '2026-04-27 21:51:19.724515', 3, 1, 'group');
INSERT INTO `message` VALUES (26, '回我的话', '2026-04-27 21:51:24.298697', 3, 1, 'group');
INSERT INTO `message` VALUES (27, '哇塞', '2026-04-29 21:53:15.366644', 2, 1, 'group');
INSERT INTO `message` VALUES (28, '哈喽', '2026-04-29 21:53:20.102171', 2, 1, 'group');
INSERT INTO `message` VALUES (29, '你好呀', '2026-04-29 21:53:22.562311', 2, 1, 'group');
INSERT INTO `message` VALUES (30, '尽然真的可以对话', '2026-04-29 21:53:27.143169', 2, 1, 'group');
INSERT INTO `message` VALUES (31, '我去了', '2026-04-29 21:53:28.523257', 2, 1, 'group');
INSERT INTO `message` VALUES (32, '好牛', '2026-04-29 21:53:30.516024', 2, 1, 'group');
INSERT INTO `message` VALUES (33, '哈哈哈哈', '2026-04-29 22:08:30.073690', 3, 1, 'group');
INSERT INTO `message` VALUES (34, '测试', '2026-04-29 22:11:20.311027', 3, 1, 'group');
INSERT INTO `message` VALUES (35, '666', '2026-04-29 22:43:34.207836', 3, 1, 'group');
INSERT INTO `message` VALUES (36, '好牛牛逼', '2026-04-29 22:43:38.525351', 3, 1, 'group');
INSERT INTO `message` VALUES (37, '哈哈哈哈', '2026-04-29 22:43:59.334715', 3, 1, 'group');
INSERT INTO `message` VALUES (38, '你好', '2026-04-29 22:44:17.308387', 2, 3, 'private');
INSERT INTO `message` VALUES (39, '你好', '2026-04-29 22:44:36.997484', 2, 3, 'private');

-- ----------------------------
-- Table structure for message_read
-- ----------------------------
DROP TABLE IF EXISTS `message_read`;
CREATE TABLE `message_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `read_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKj4060r27lsbcvmv2yfrfdg97t`(`message_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message_read
-- ----------------------------
INSERT INTO `message_read` VALUES (1, 1, '2026-04-16 21:57:36.032821', 2);
INSERT INTO `message_read` VALUES (2, 1, '2026-04-18 10:08:50.276606', 1);
INSERT INTO `message_read` VALUES (3, 2, '2026-04-16 21:58:08.054526', 2);
INSERT INTO `message_read` VALUES (4, 2, '2026-04-18 10:08:50.276606', 1);
INSERT INTO `message_read` VALUES (5, 3, '2026-04-18 10:45:31.678546', 2);
INSERT INTO `message_read` VALUES (6, 3, '2026-04-18 10:45:47.164621', 1);
INSERT INTO `message_read` VALUES (7, 4, '2026-04-18 14:23:11.018259', 2);
INSERT INTO `message_read` VALUES (8, 4, '2026-04-18 14:23:17.685114', 1);
INSERT INTO `message_read` VALUES (9, 5, '2026-04-18 14:23:23.898259', 1);
INSERT INTO `message_read` VALUES (10, 5, '2026-04-18 14:23:27.738848', 2);
INSERT INTO `message_read` VALUES (11, 6, '2026-04-18 20:19:13.479634', 2);
INSERT INTO `message_read` VALUES (12, 6, '2026-04-18 20:21:45.951264', 1);
INSERT INTO `message_read` VALUES (13, 7, '2026-04-18 20:22:10.005570', 1);
INSERT INTO `message_read` VALUES (14, 7, '2026-04-18 20:22:10.056870', 2);
INSERT INTO `message_read` VALUES (15, 8, '2026-04-18 20:22:26.440402', 1);
INSERT INTO `message_read` VALUES (16, 8, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (17, 9, '2026-04-18 20:22:36.151983', 1);
INSERT INTO `message_read` VALUES (18, 9, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (19, 10, '2026-04-18 20:22:41.360261', 1);
INSERT INTO `message_read` VALUES (20, 10, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (21, 11, '2026-04-18 20:22:47.200088', 1);
INSERT INTO `message_read` VALUES (22, 11, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (23, 12, '2026-04-18 20:22:50.866358', 1);
INSERT INTO `message_read` VALUES (24, 12, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (25, 13, '2026-04-18 22:43:17.827312', 1);
INSERT INTO `message_read` VALUES (26, 13, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (27, 14, '2026-04-18 22:43:20.857057', 1);
INSERT INTO `message_read` VALUES (28, 14, '2026-04-18 22:45:53.941395', 2);
INSERT INTO `message_read` VALUES (29, 15, '2026-04-19 14:30:36.974839', 3);
INSERT INTO `message_read` VALUES (30, 15, '2026-04-19 14:31:28.680827', 1);
INSERT INTO `message_read` VALUES (31, 16, '2026-04-19 14:31:47.370936', 3);
INSERT INTO `message_read` VALUES (32, 16, '2026-04-19 14:31:47.391812', 1);
INSERT INTO `message_read` VALUES (33, 17, '2026-04-19 14:32:04.375285', 1);
INSERT INTO `message_read` VALUES (34, 17, '2026-04-19 14:32:04.406428', 2);
INSERT INTO `message_read` VALUES (35, 18, '2026-04-19 14:32:09.136492', 1);
INSERT INTO `message_read` VALUES (36, 18, '2026-04-19 14:32:09.171631', 2);
INSERT INTO `message_read` VALUES (37, 19, '2026-04-19 14:32:17.519209', 1);
INSERT INTO `message_read` VALUES (38, 19, '2026-04-19 14:32:19.503046', 3);
INSERT INTO `message_read` VALUES (39, 20, '2026-04-19 14:32:25.735013', 1);
INSERT INTO `message_read` VALUES (40, 20, '2026-04-19 14:32:25.763809', 3);
INSERT INTO `message_read` VALUES (41, 21, '2026-04-19 14:32:28.716355', 1);
INSERT INTO `message_read` VALUES (42, 21, '2026-04-19 14:32:28.745056', 3);
INSERT INTO `message_read` VALUES (43, 22, '2026-04-19 14:32:30.380430', 1);
INSERT INTO `message_read` VALUES (45, 24, '2026-04-27 21:51:14.228929', 3);
INSERT INTO `message_read` VALUES (46, 25, '2026-04-27 21:51:19.727515', 3);
INSERT INTO `message_read` VALUES (47, 26, '2026-04-27 21:51:24.302259', 3);
INSERT INTO `message_read` VALUES (48, 27, '2026-04-29 21:53:15.370160', 2);
INSERT INTO `message_read` VALUES (49, 27, '2026-04-29 21:53:15.372158', 3);
INSERT INTO `message_read` VALUES (50, 28, '2026-04-29 21:53:20.105171', 2);
INSERT INTO `message_read` VALUES (51, 28, '2026-04-29 21:53:20.107169', 3);
INSERT INTO `message_read` VALUES (52, 29, '2026-04-29 21:53:22.564312', 2);
INSERT INTO `message_read` VALUES (53, 29, '2026-04-29 21:53:22.567817', 3);
INSERT INTO `message_read` VALUES (54, 30, '2026-04-29 21:53:27.145168', 2);
INSERT INTO `message_read` VALUES (55, 30, '2026-04-29 21:53:27.146169', 3);
INSERT INTO `message_read` VALUES (56, 31, '2026-04-29 21:53:28.525765', 2);
INSERT INTO `message_read` VALUES (57, 31, '2026-04-29 21:53:28.527271', 3);
INSERT INTO `message_read` VALUES (58, 32, '2026-04-29 21:53:30.519212', 2);
INSERT INTO `message_read` VALUES (59, 32, '2026-04-29 21:53:30.520210', 3);
INSERT INTO `message_read` VALUES (60, 33, '2026-04-29 22:08:30.075689', 2);
INSERT INTO `message_read` VALUES (61, 33, '2026-04-29 22:08:30.076888', 3);
INSERT INTO `message_read` VALUES (62, 34, '2026-04-29 22:11:20.314026', 2);
INSERT INTO `message_read` VALUES (63, 34, '2026-04-29 22:11:20.315024', 3);
INSERT INTO `message_read` VALUES (64, 35, '2026-04-29 22:43:34.211081', 2);
INSERT INTO `message_read` VALUES (65, 35, '2026-04-29 22:43:34.213095', 3);
INSERT INTO `message_read` VALUES (66, 36, '2026-04-29 22:43:38.527362', 2);
INSERT INTO `message_read` VALUES (67, 36, '2026-04-29 22:43:38.528364', 3);
INSERT INTO `message_read` VALUES (68, 37, '2026-04-29 22:43:59.336033', 2);
INSERT INTO `message_read` VALUES (69, 37, '2026-04-29 22:43:59.338318', 3);
INSERT INTO `message_read` VALUES (70, 38, '2026-04-29 22:44:17.309771', 2);
INSERT INTO `message_read` VALUES (71, 38, '2026-04-29 23:03:49.998849', 3);
INSERT INTO `message_read` VALUES (72, 39, '2026-04-29 22:44:36.999486', 2);
INSERT INTO `message_read` VALUES (73, 39, '2026-04-29 23:03:49.998849', 3);

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容（支持Markdown）',
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, 'nihao', '嘿嘿大家好', 1, 1, '2026-04-12 14:50:55', '2026-04-12 14:50:55');
INSERT INTO `post` VALUES (3, '你好你好你好', '我是管理员', 3, 1, '2026-04-29 21:52:40', '2026-04-29 21:52:40');

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `handle_result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `handled_at` datetime(6) NULL DEFAULT NULL,
  `handled_by` bigint NULL DEFAULT NULL,
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reporter_id` bigint NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------

-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `replacement` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `word` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKdmi0m8ohbw73u0uyplv8l0ox6`(`word` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensitive_word
-- ----------------------------
INSERT INTO `sensitive_word` VALUES (2, '2026-04-19 16:27:52.959187', 'WARN', '哈哈哈', '尼玛');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKbqy406dtsr7j7d6fawi1ckyn1`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '2026-04-18 23:29:42', '系统管理员', 'ADMIN');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKmjo3l593cr1835oo1asuch8u`(`user_id` ASC, `role_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 3, '2026-04-18 23:29:58');

-- ----------------------------
-- Table structure for tournament_match
-- ----------------------------
DROP TABLE IF EXISTS `tournament_match`;
CREATE TABLE `tournament_match`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_id` bigint NOT NULL COMMENT '关联赛事ID',
  `round` int NOT NULL COMMENT '轮次(1=首轮, 最大轮=决赛)',
  `match_order` int NOT NULL COMMENT '该轮中的序号(从0开始)',
  `phase` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `team1_id` bigint NULL DEFAULT NULL COMMENT '队伍1(event_registration.id)',
  `team2_id` bigint NULL DEFAULT NULL COMMENT '队伍2(event_registration.id)',
  `winner_id` bigint NULL DEFAULT NULL COMMENT '胜者(event_registration.id)',
  `score1` int NULL DEFAULT NULL COMMENT '队伍1比分',
  `score2` int NULL DEFAULT NULL COMMENT '队伍2比分',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `next_match_id` bigint NULL DEFAULT NULL COMMENT '胜者晋级到的比赛ID',
  `next_match_slot` int NULL DEFAULT NULL COMMENT '进入下一场的槽位(1或2)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_match_event`(`event_id` ASC) USING BTREE,
  INDEX `idx_match_round`(`event_id` ASC, `round` ASC) USING BTREE,
  INDEX `idx_match_status`(`event_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '锦标赛对阵图比赛表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tournament_match
-- ----------------------------
INSERT INTO `tournament_match` VALUES (16, 1, 4, 0, 'KNOCKOUT', NULL, NULL, NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (17, 1, 3, 0, 'KNOCKOUT', NULL, 11, 13, NULL, NULL, NULL, 'PENDING', 16, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (18, 1, 3, 1, 'KNOCKOUT', NULL, NULL, NULL, NULL, NULL, NULL, 'PENDING', 16, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (19, 1, 2, 0, 'KNOCKOUT', NULL, 1, 11, 11, NULL, NULL, 'COMPLETED', 17, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (20, 1, 2, 1, 'KNOCKOUT', NULL, 13, 19, 13, NULL, NULL, 'COMPLETED', 17, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (21, 1, 2, 2, 'KNOCKOUT', NULL, 20, 14, NULL, NULL, NULL, 'PENDING', 18, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (22, 1, 2, 3, 'KNOCKOUT', NULL, 10, 8, NULL, NULL, NULL, 'PENDING', 18, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (23, 1, 1, 0, 'KNOCKOUT', NULL, 1, 7, 1, NULL, NULL, 'COMPLETED', 19, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (24, 1, 1, 1, 'KNOCKOUT', NULL, 9, 11, 11, NULL, NULL, 'COMPLETED', 19, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (25, 1, 1, 2, 'KNOCKOUT', NULL, 13, 15, 13, NULL, NULL, 'COMPLETED', 20, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (26, 1, 1, 3, 'KNOCKOUT', NULL, 17, 19, 19, NULL, NULL, 'COMPLETED', 20, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (27, 1, 1, 4, 'KNOCKOUT', NULL, 20, 18, 20, NULL, NULL, 'COMPLETED', 21, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (28, 1, 1, 5, 'KNOCKOUT', NULL, 16, 14, 14, NULL, NULL, 'COMPLETED', 21, 2, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (29, 1, 1, 6, 'KNOCKOUT', NULL, 12, 10, 10, NULL, NULL, 'COMPLETED', 22, 1, '2026-05-09 23:55:21');
INSERT INTO `tournament_match` VALUES (30, 1, 1, 7, 'KNOCKOUT', NULL, 8, 6, 8, NULL, NULL, 'COMPLETED', 22, 2, '2026-05-09 23:55:21');

-- ----------------------------
-- Table structure for tournament_standings
-- ----------------------------
DROP TABLE IF EXISTS `tournament_standings`;
CREATE TABLE `tournament_standings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_id` bigint NOT NULL COMMENT '关联赛事ID',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `registration_id` bigint NOT NULL COMMENT '队伍(event_registration.id)',
  `wins` int NOT NULL DEFAULT 0 COMMENT '胜场',
  `losses` int NOT NULL DEFAULT 0 COMMENT '负场',
  `points_scored` int NOT NULL DEFAULT 0 COMMENT '总得分',
  `points_lost` int NOT NULL DEFAULT 0 COMMENT '总失分',
  `rank` int NULL DEFAULT NULL COMMENT '小组内排名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_standings_event_group`(`event_id` ASC, `group_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '循环赛积分榜' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tournament_standings
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '19817115293', '$2a$10$nUqCzsl7Qtf3jTvifoB04.9/f2pctwXonkuGHC.1nCZO6aZjtAj7y', 'lihao', '1', '', '2026-04-12 14:27:50');
INSERT INTO `user` VALUES (2, '19855823027', '$2a$10$55pYgRq75yHIKI20iTI0W.FwwUfTt8Ays2JpJTOITk9o9bl2T0wWu', 'leehao', '2', '你好，我是李昊', '2026-04-15 22:07:57');
INSERT INTO `user` VALUES (3, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', '3', '系统管理员', '2026-04-18 23:29:42');

-- ----------------------------
-- Table structure for user_privacy
-- ----------------------------
DROP TABLE IF EXISTS `user_privacy`;
CREATE TABLE `user_privacy`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follow_list_visible` bit(1) NOT NULL,
  `follower_list_visible` bit(1) NOT NULL,
  `friends_only_receive` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKlewbvecbe8bjdvsl4p1ih3yyi`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_privacy
-- ----------------------------
INSERT INTO `user_privacy` VALUES (1, b'1', b'1', b'0', 2);
INSERT INTO `user_privacy` VALUES (2, b'1', b'1', b'0', 1);
INSERT INTO `user_privacy` VALUES (3, b'1', b'1', b'0', 3);

-- ----------------------------
-- Table structure for user_status
-- ----------------------------
DROP TABLE IF EXISTS `user_status`;
CREATE TABLE `user_status`  (
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `disabled` bit(1) NOT NULL,
  `disabled_at` datetime(6) NULL DEFAULT NULL,
  `disabled_by` bigint NULL DEFAULT NULL,
  `disabled_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_status
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
