/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : campuslink

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 11/11/2025 15:59:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `club_id` bigint NOT NULL COMMENT '社团ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `max_participants` int NULL DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int NOT NULL DEFAULT 0 COMMENT '当前参与人数',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '参与奖励积分',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已取消',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_club_id`(`club_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  CONSTRAINT `fk_activity_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------

-- ----------------------------
-- Table structure for activity_participant
-- ----------------------------
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant`  (
  `ap_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_signed_in` tinyint NOT NULL DEFAULT 0 COMMENT '是否已签到：0-否，1-是',
  `signed_in_at` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`ap_id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_activity_participant_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`activity_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_activity_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动参与表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_participant
-- ----------------------------

-- ----------------------------
-- Table structure for answer
-- ----------------------------
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer`  (
  `a_id` bigint NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` bigint NOT NULL COMMENT '问题ID',
  `responder_id` bigint NOT NULL COMMENT '回答者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回答内容',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `is_accepted` tinyint NOT NULL DEFAULT 0 COMMENT '是否被采纳：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`a_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_responder_id`(`responder_id` ASC) USING BTREE,
  INDEX `idx_is_accepted`(`is_accepted` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`q_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_answer_responder` FOREIGN KEY (`responder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of answer
-- ----------------------------
INSERT INTO `answer` VALUES (1, 1, 4, '学习数据结构建议从基础的线性表开始，然后学习栈、队列、树、图等。推荐《数据结构与算法分析》这本书，同时多刷LeetCode上的题目。', NULL, 20, 1, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (2, 1, 3, '我的经验是理论和实践结合，看书的同时一定要自己动手实现各种数据结构，这样理解会更深刻。', NULL, 15, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (3, 1, 6, '可以先学C语言基础，然后再学数据结构。推荐浙大的数据结构MOOC课程，讲得很好。', NULL, 10, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (4, 3, 3, '图书馆自习室可以通过学校的\"智慧校园\"APP预约，每天早上8点开放预约，先到先得。', NULL, 8, 1, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (5, 3, 4, '也可以直接去图书馆前台登记，但高峰期可能没有位置。', NULL, 5, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');

-- ----------------------------
-- Table structure for club
-- ----------------------------
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club`  (
  `club_id` bigint NOT NULL AUTO_INCREMENT COMMENT '社团ID',
  `club_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '社团名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '社团简介',
  `logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '社团Logo',
  `school_id` bigint NOT NULL COMMENT '所属学校ID',
  `founder_id` bigint NOT NULL COMMENT '创建者ID',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已解散，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`club_id`) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_founder_id`(`founder_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_club_founder` FOREIGN KEY (`founder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_club_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社团表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club
-- ----------------------------

-- ----------------------------
-- Table structure for club_member
-- ----------------------------
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member`  (
  `cm_id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `club_id` bigint NOT NULL COMMENT '社团ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('member','admin','founder') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'member' COMMENT '角色',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`cm_id`) USING BTREE,
  UNIQUE INDEX `uk_club_user`(`club_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_club_member_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_club_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社团成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_member
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论对象类型（resource/question/answer等）',
  `target_id` bigint NOT NULL COMMENT '评论对象ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID（用于回复）',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 43, 'resource', 1, '这个资源非常有用，帮助我通过了考试！感谢分享。', NULL, 4, 0, '2025-11-09 14:06:35');
INSERT INTO `comment` VALUES (2, 44, 'resource', 1, '同意楼上，这个资料整理得很详细，值得下载！', NULL, 1, 1, '2025-11-09 14:06:41');
INSERT INTO `comment` VALUES (3, 43, 'resource', 1, '是的，我也觉得整理得很好，特别是重点部分标注得很清楚。', 2, 0, 1, '2025-11-09 14:06:45');
INSERT INTO `comment` VALUES (4, 45, 'resource', 1, '确实不错，我已经下载了，很实用！', 2, 0, 1, '2025-11-09 14:06:50');
INSERT INTO `comment` VALUES (5, 44, 'question', 1, '这个问题我也遇到过，建议先检查一下配置文件。', NULL, 0, 1, '2025-11-09 14:06:52');
INSERT INTO `comment` VALUES (6, 43, 'answer', 1, '这个回答很详细，解决了我的疑惑！', NULL, 0, 1, '2025-11-09 14:06:54');
INSERT INTO `comment` VALUES (7, 45, 'activity', 1, '这个活动很有意思，期待参加！', NULL, 0, 1, '2025-11-09 14:06:56');
INSERT INTO `comment` VALUES (11, 43, 'resource', 1, '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789这是超过1000字符的内容用于测试验证规则是否生效', NULL, 0, 1, '2025-11-09 14:09:04');
INSERT INTO `comment` VALUES (12, 44, 'resource', 1, '补充一下，还有另一个重点章节也很重要。', 3, 0, 1, '2025-11-09 14:13:01');
INSERT INTO `comment` VALUES (13, 45, 'resource', 1, '同意，那个章节的确很关键，考试必考。', 3, 0, 1, '2025-11-09 14:13:04');
INSERT INTO `comment` VALUES (14, 43, 'activity', 2, '这个社团活动很赞！', NULL, 0, 1, '2025-11-09 14:13:12');
INSERT INTO `comment` VALUES (15, 44, 'activity', 2, '活动时间是什么时候？', NULL, 0, 1, '2025-11-09 14:13:14');
INSERT INTO `comment` VALUES (16, 45, 'activity', 2, '地点在哪里呢？需要报名吗？', NULL, 0, 1, '2025-11-09 14:13:15');

-- ----------------------------
-- Table structure for download_log
-- ----------------------------
DROP TABLE IF EXISTS `download_log`;
CREATE TABLE `download_log`  (
  `dl_id` bigint NOT NULL AUTO_INCREMENT COMMENT '下载记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `points_cost` int NOT NULL DEFAULT 0 COMMENT '消耗积分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  PRIMARY KEY (`dl_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_download_log_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_download_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '下载记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of download_log
-- ----------------------------
INSERT INTO `download_log` VALUES (1, 38, 1, 5, '2025-11-09 13:51:57');
INSERT INTO `download_log` VALUES (2, 38, 2, 10, '2025-11-09 13:52:06');
INSERT INTO `download_log` VALUES (3, 38, 3, 3, '2025-11-09 13:52:30');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏对象类型（task/resource/question等）',
  `target_id` bigint NOT NULL COMMENT '收藏对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 48, 'task', 2, '2025-11-11 15:49:09');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `m_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-文件',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`m_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (2, 25, 2, 'https://example.com/image.jpg', 2, 0, '2025-11-08 20:59:24');
INSERT INTO `message` VALUES (3, 25, 2, 'https://example.com/document.pdf', 3, 0, '2025-11-08 20:59:27');
INSERT INTO `message` VALUES (4, 26, 1, '好的,我有一些学习资料可以分享给你', 1, 0, '2025-11-08 20:59:30');
INSERT INTO `message` VALUES (5, 25, 2, '太好了,非常感谢!', 1, 0, '2025-11-08 20:59:32');
INSERT INTO `message` VALUES (6, 25, 1, '给自己发消息', 1, 0, '2025-11-08 21:00:30');
INSERT INTO `message` VALUES (7, 25, 3, '你好,用户3', 1, 0, '2025-11-08 21:00:40');
INSERT INTO `message` VALUES (8, 25, 2, '消息1', 1, 0, '2025-11-08 21:00:51');
INSERT INTO `message` VALUES (9, 25, 2, '消息2', 1, 0, '2025-11-08 21:00:53');
INSERT INTO `message` VALUES (10, 25, 2, '消息3', 1, 0, '2025-11-08 21:00:54');
INSERT INTO `message` VALUES (11, 25, 2, '消息4', 1, 0, '2025-11-08 21:00:55');
INSERT INTO `message` VALUES (12, 25, 2, '消息5', 1, 0, '2025-11-08 21:00:57');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `notify_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型（system/answer/task/activity等）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联对象类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (1, 1, '系统维护通知', '系统将于今晚22:00-23:00进行维护升级，届时将暂停服务，请提前做好准备。', 'system', NULL, NULL, 0, '2025-11-08 21:15:26');
INSERT INTO `notification` VALUES (2, 1, '你的问题有新回答', '用户「通知测试用户2」回答了你的问题「如何学习Spring Boot」', 'answer', 'question', 1, 0, '2025-11-08 21:15:32');
INSERT INTO `notification` VALUES (3, 2, '新任务分配', '你被分配了新任务「完成图书馆志愿服务」，请在本周五前完成。', 'task', 'task', 1, 0, '2025-11-08 21:15:35');
INSERT INTO `notification` VALUES (4, 1, '活动即将开始', '你报名的活动「校园马拉松」将于明天上午8:00开始，请准时参加。', 'activity', 'activity', 1, 0, '2025-11-08 21:15:38');
INSERT INTO `notification` VALUES (5, 2, '你收到了新私信', '用户「通知测试用户1」给你发送了私信', 'message', 'message', 1, 0, '2025-11-08 21:15:40');
INSERT INTO `notification` VALUES (6, 1, '举报处理结果', '你举报的内容已经处理完毕，感谢你对平台的监督。', 'report', 'report', 1, 0, '2025-11-08 21:15:45');
INSERT INTO `notification` VALUES (7, 3, '社团活动通知', '计算机协会将于本周六举行技术分享会，欢迎参加。', 'club', 'club', 1, 0, '2025-11-08 21:15:47');
INSERT INTO `notification` VALUES (8, 1, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (9, 2, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (10, 3, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (11, 4, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (12, 5, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (13, 6, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (14, 18, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (15, 19, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (16, 20, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (17, 21, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (18, 22, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (19, 23, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (20, 24, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (21, 25, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (22, 26, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (23, 27, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (24, 28, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (26, 30, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (27, 31, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (28, 32, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (29, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:15:54');
INSERT INTO `notification` VALUES (30, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:16:07');
INSERT INTO `notification` VALUES (31, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:16:35');

-- ----------------------------
-- Table structure for points_log
-- ----------------------------
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points_change` int NOT NULL COMMENT '积分变化（正数为增加，负数为减少）',
  `points_after` int NOT NULL COMMENT '变化后的积分',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变化原因',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联对象类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_points_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_log
-- ----------------------------
INSERT INTO `points_log` VALUES (1, 23, -10, 90, '发布任务：测试任务 - 用于验证积分日志', 'task', 7, '2025-11-08 20:27:10');
INSERT INTO `points_log` VALUES (2, 24, 10, 110, '完成任务：测试任务 - 用于验证积分日志', 'task', 7, '2025-11-08 20:27:42');
INSERT INTO `points_log` VALUES (3, 23, -5, 85, '发布任务：测试任务2 - 用于取消', 'task', 8, '2025-11-08 20:27:53');
INSERT INTO `points_log` VALUES (4, 23, 5, 90, '取消任务退还积分：测试任务2 - 用于取消', 'task', 8, '2025-11-08 20:27:55');

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `q_id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题内容',
  `asker_id` bigint NOT NULL COMMENT '提问者ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类（学习/生活/技术等）',
  `tags` json NULL COMMENT '标签（JSON数组）',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `ai_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI生成的答案',
  `ai_generated_at` datetime NULL DEFAULT NULL COMMENT 'AI答案生成时间',
  `views` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `answer_count` int NOT NULL DEFAULT 0 COMMENT '回答数量',
  `is_solved` tinyint NOT NULL DEFAULT 0 COMMENT '是否已解决：0-否，1-是',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`q_id`) USING BTREE,
  INDEX `idx_asker_id`(`asker_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_is_solved`(`is_solved` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_question_asker` FOREIGN KEY (`asker_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '问题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (1, '如何学习数据结构？', '我是大一新生，想学习数据结构，有什么好的建议吗？需要掌握哪些基础知识？', 5, '学习', '[\"数据结构\", \"学习方法\"]', NULL, NULL, NULL, 150, 3, 1, 10, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (2, 'Python和Java哪个更适合初学者？', '我想学习编程，但不知道选Python还是Java，请大家给点建议', 5, '技术', '[\"Python\", \"Java\", \"编程语言\"]', NULL, NULL, NULL, 200, 5, 0, 5, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (3, '图书馆自习室怎么预约？', '请问学校图书馆的自习室需要提前预约吗？怎么预约？', 5, '生活', '[\"图书馆\", \"自习室\"]', NULL, NULL, NULL, 80, 2, 1, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (4, '考研需要准备多久？', '打算考研，想问问学长学姐们一般需要准备多长时间？', 2, '学习', '[\"考研\", \"备考\"]', NULL, NULL, NULL, 300, 8, 0, 15, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` bigint NOT NULL COMMENT '举报者ID',
  `report_type` tinyint NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户，4-活动',
  `target_id` bigint NOT NULL COMMENT '举报对象ID',
  `reason_type` tinyint NOT NULL COMMENT '举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报详细描述',
  `evidence_images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报凭证图片(多张用逗号分隔)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已处理，2-已驳回',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `fk_report_handler`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------
INSERT INTO `report` VALUES (1, 27, 1, 1, 1, '这个帖子是广告垃圾信息,请尽快处理', 'https://example.com/evidence1.jpg,https://example.com/evidence2.jpg', 1, 28, '经核实,该内容确实违反社区规范,已对相关内容进行删除处理,并对发布者进行警告', '2025-11-08 21:03:43', '2025-11-08 21:04:37');
INSERT INTO `report` VALUES (2, 27, 2, 1, 2, '该评论包含违规内容,涉及敏感话题', 'https://example.com/evidence3.jpg', 2, 28, '经核实,该内容未发现明显违规,驳回此次举报', '2025-11-08 21:03:45', '2025-11-08 21:04:40');
INSERT INTO `report` VALUES (4, 27, 4, 1, 4, '该活动未经授权使用了我们社团的名称和logo', '', 0, NULL, NULL, '2025-11-08 21:03:51', NULL);
INSERT INTO `report` VALUES (5, 27, 1, 2, 5, '其他原因:该帖子内容与板块主题不符', '', 1, 28, '已核实并删除违规内容,感谢您的举报', '2025-11-08 21:03:53', '2025-11-08 21:06:11');
INSERT INTO `report` VALUES (6, 27, 1, 99999, 1, '测试举报不存在的帖子', '', 0, NULL, NULL, '2025-11-08 21:04:46', NULL);
INSERT INTO `report` VALUES (7, 27, 1, 1, 1, '重复举报测试', '', 0, NULL, NULL, '2025-11-08 21:05:03', NULL);
INSERT INTO `report` VALUES (8, 27, 1, 5, 2, '该帖子包含不当内容,请管理员审核', 'https://example.com/evidence.jpg', 0, NULL, NULL, '2025-11-08 21:05:58', NULL);

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `r_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资源描述',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL（OSS）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型（pdf/doc/ppt等）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类（课件/试题/笔记等）',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `score` int NOT NULL DEFAULT 0 COMMENT '所需积分',
  `downloads` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`r_id`) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_resource_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (1, '数据结构课件-第一章', '数据结构基础知识，包括线性表、栈、队列等内容', 2, 'https://oss.example.com/resources/ds-chapter1.pdf', '数据结构-第一章.pdf', 2048576, 'pdf', '课件', '数据结构', NULL, 5, 155, 37, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (2, '操作系统期末复习资料', '操作系统期末考试重点总结，包含历年真题', 3, 'https://oss.example.com/resources/os-review.pdf', '操作系统复习.pdf', 3145728, 'pdf', '试题', '操作系统', NULL, 10, 203, 50, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (3, '计算机网络笔记', '计算机网络课程笔记，涵盖TCP/IP协议栈', 4, 'https://oss.example.com/resources/network-notes.pdf', '网络笔记.pdf', 1572864, 'pdf', '笔记', '计算机网络', NULL, 3, 82, 20, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (4, '算法设计与分析PPT', '算法课程PPT，包含动态规划、贪心算法等', 5, 'https://oss.example.com/resources/algorithm.pptx', '算法设计.pptx', 4194304, 'pptx', '课件', '算法设计与分析', NULL, 8, 124, 28, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (5, '数据库系统概论习题集', '数据库课程习题及答案解析', 6, 'https://oss.example.com/resources/db-exercises.pdf', '数据库习题.pdf', 2621440, 'pdf', '试题', '数据库系统概论', NULL, 6, 99, 18, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (13, '数据结构与算法 - 期末复习资料', '包含所有章节的知识点总结、经典例题和历年真题解析。适合期末复习使用。', 37, 'https://campuslink-oss.example.com/resources/data-structure-review.pdf', '数据结构期末复习.pdf', 5242880, 'pdf', '课件', '数据结构与算法', NULL, 5, 0, 0, 1, NULL, '2025-11-09 13:11:15', '2025-11-09 13:11:42');
INSERT INTO `resource` VALUES (14, '操作系统 - 进程管理PPT', '操作系统第三章进程管理完整PPT，包含详细的注释和示例代码。', 37, 'https://campuslink-oss.example.com/resources/os-process.pptx', '进程管理.pptx', 3145728, 'pptx', '课件', '操作系统', NULL, 3, 0, 0, 0, NULL, '2025-11-09 13:12:01', '2025-11-09 13:12:01');
INSERT INTO `resource` VALUES (15, '高等数学 - 2024年期中考试试卷', '2024年春季学期高等数学期中考试真题及详细答案解析。', 37, 'https://campuslink-oss.example.com/resources/math-midterm-2024.pdf', '高数期中试卷2024.pdf', 2097152, 'pdf', '试题', '高等数学', NULL, 8, 0, 0, 2, '资源内容不符合平台规范，包含敏感信息，请修改后重新上传。', '2025-11-09 13:12:03', '2025-11-09 13:12:21');
INSERT INTO `resource` VALUES (16, '完整流程测试资源', '用于测试完整审核流程的资源', 37, 'https://campuslink-oss.example.com/resources/workflow-test.pdf', 'workflow-test.pdf', 2048000, 'pdf', '其他', '测试课程', NULL, 3, 1, 1, 1, NULL, '2025-11-09 13:13:17', '2025-11-09 13:13:42');
INSERT INTO `resource` VALUES (17, '测试拒绝流程资源', '这个资源将被管理员拒绝', 37, 'https://campuslink-oss.example.com/resources/reject-test.pdf', 'reject-test.pdf', 1024000, 'pdf', '其他', '测试课程', NULL, 2, 0, 0, 2, '资源质量不达标，内容过于简单，不具备分享价值。建议补充详细内容后重新提交。', '2025-11-09 13:14:06', '2025-11-09 13:14:16');

-- ----------------------------
-- Table structure for school
-- ----------------------------
DROP TABLE IF EXISTS `school`;
CREATE TABLE `school`  (
  `school_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  `school_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校Logo',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`school_id`) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school
-- ----------------------------
INSERT INTO `school` VALUES (1, '北京大学(更新)', '北京市', '北京市', '北京市海淀区颐和园路5号(新地址)', 'https://www.pku.edu.cn/logo.png', 1, '2025-11-09 13:37:46');
INSERT INTO `school` VALUES (2, '清华大学', '北京市', '北京市', '北京市海淀区清华园1号', 'https://www.tsinghua.edu.cn/new-logo.png', 1, '2025-11-09 13:37:48');
INSERT INTO `school` VALUES (3, '复旦大学', '上海市', '上海市', '上海市杨浦区邯郸路220号', 'https://www.fudan.edu.cn/logo.png', 1, '2025-11-09 13:37:50');
INSERT INTO `school` VALUES (4, '浙江大学', '浙江省', '杭州市', '浙江省杭州市西湖区余杭塘路866号', 'https://www.zju.edu.cn/logo.png', 1, '2025-11-09 13:37:52');
INSERT INTO `school` VALUES (16, '南京大学', '江苏省', '南京市', '江苏省南京市栖霞区仙林大道163号', 'https://www.nju.edu.cn/logo.png', 1, '2025-11-09 13:41:56');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'points.upload_resource', '10', '上传资源获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (2, 'points.download_resource', '-5', '下载资源消耗的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (3, 'points.answer_question', '5', '回答问题获得的积分', '2025-11-08 19:09:44', '2025-11-09 14:20:04');
INSERT INTO `system_config` VALUES (4, 'points.answer_accepted', '20', '回答被采纳获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (5, 'points.ask_question', '-2', '提问消耗的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (6, 'points.complete_task', '10', '完成任务获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (7, 'points.daily_signin', '15', '每日签到获得的积分数（已调整）', '2025-11-08 19:09:44', '2025-11-09 14:19:38');
INSERT INTO `system_config` VALUES (8, 'points.activity_signin', '10', '活动签到获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (9, 'level.threshold_1', '0', '等级1所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (10, 'level.threshold_2', '100', '等级2所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (11, 'level.threshold_3', '300', '等级3所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (12, 'level.threshold_4', '600', '等级4所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (13, 'level.threshold_5', '1000', '等级5所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (14, 'level.threshold_6', '1500', '等级6所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (15, 'level.threshold_7', '2100', '等级7所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (16, 'level.threshold_8', '2800', '等级8所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (17, 'level.threshold_9', '3600', '等级9所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (18, 'level.threshold_10', '5000', '等级10所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (19, 'upload.max_file_size', '209715200', '上传文件最大大小（字节），调整为200MB', '2025-11-08 19:09:44', '2025-11-09 14:20:12');
INSERT INTO `system_config` VALUES (20, 'upload.allowed_types', 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png', '允许上传的文件类型', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (21, 'resource.review_required', 'true', '资源是否需要审核', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (22, 'message.max_length', '1000', '私信最大长度', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (23, 'comment.max_length', '500', '评论最大长度', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (24, 'task.max_reward_points', '100', '任务最大悬赏积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (25, 'question.max_reward_points', '50', '问题最大悬赏积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (26, 'ai.enabled', 'true', 'AI功能是否启用', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (27, 'ai.auto_answer', 'true', '是否自动生成AI答案', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (28, 'ai.answer_delay', '5', 'AI答案生成延迟（秒）', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (29, 'system.name', 'CampusLink 校园平台', '系统名称（批量更新）', '2025-11-09 14:18:24', '2025-11-09 14:19:48');
INSERT INTO `system_config` VALUES (30, 'points.daily_signin1', '10', '每日签到获得的积分数', '2025-11-09 14:18:43', '2025-11-09 14:18:43');
INSERT INTO `system_config` VALUES (31, 'upload.allowed_file_types', 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png', '允许上传的文件类型', '2025-11-09 14:18:54', '2025-11-09 14:18:54');
INSERT INTO `system_config` VALUES (33, 'email.smtp_port', '587', 'SMTP服务器端口', '2025-11-09 14:19:48', '2025-11-09 14:19:48');
INSERT INTO `system_config` VALUES (34, 'points.answer_adopted', '30', '回答被采纳获得的积分（已提高奖励）', '2025-11-09 14:20:04', '2025-11-09 14:20:09');
INSERT INTO `system_config` VALUES (35, 'points.publish_task', '0', '发布任务消耗的积分', '2025-11-09 14:20:04', '2025-11-09 14:20:04');
INSERT INTO `system_config` VALUES (36, 'upload.image_max_size', '5242880', '图片上传最大大小（5MB）', '2025-11-09 14:20:11', '2025-11-09 14:20:11');
INSERT INTO `system_config` VALUES (37, '1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890', '测试值', '100字符的配置键', '2025-11-09 14:20:18', '2025-11-09 14:20:18');
INSERT INTO `system_config` VALUES (38, 'test.max_desc', '测试值', '这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制', '2025-11-09 14:20:20', '2025-11-09 14:20:20');
INSERT INTO `system_config` VALUES (39, 'test.long_value', '这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。', '测试长配置值', '2025-11-09 14:20:25', '2025-11-09 14:20:25');
INSERT INTO `system_config` VALUES (40, 'test.json_config', '{\"feature1\": true, \"feature2\": false, \"maxCount\": 100}', 'JSON格式的配置', '2025-11-09 14:20:27', '2025-11-09 14:20:27');

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID（主键）',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称（不含#号）',
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称（含#号，如：#考研资料）',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数（热度）',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签分类：学习/生活/技术/娱乐等',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `idx_use_count`(`use_count` DESC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '考研资料', '#考研资料', 120, '学习', '考研相关的资料和经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (2, '学习打卡', '#学习打卡', 95, '学习', '记录和分享学习进度', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (3, 'AI问答', '#AI问答', 88, '技术', 'AI和人工智能相关问答', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (4, 'Python基础', '#Python基础', 76, '技术', 'Python编程基础知识', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (5, '数据结构', '#数据结构', 82, '学习', '数据结构与算法相关内容', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (6, '英语四六级', '#英语四六级', 110, '学习', '英语四六级考试资料和经验', 1, '2025-11-10 16:52:39', '2025-11-10 18:58:37');
INSERT INTO `tag` VALUES (7, '算法刷题', '#算法刷题', 68, '技术', '算法题目和解题思路', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (8, '前端开发', '#前端开发', 72, '技术', '前端技术和开发经验', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (9, 'Java学习', '#Java学习', 64, '技术', 'Java编程学习资料', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (10, '求职面试', '#求职面试', 58, '生活', '求职和面试经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (11, '实习经验', '#实习经验', 52, '生活', '实习相关经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (12, '校园生活', '#校园生活', 90, '生活', '校园日常生活分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');

-- ----------------------------
-- Table structure for tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `tag_relation`;
CREATE TABLE `tag_relation`  (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键）',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联对象类型：question/resource/task',
  `target_id` bigint NOT NULL COMMENT '关联对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_target`(`tag_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag_relation
-- ----------------------------

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `t_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务内容',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型（errand/borrow/sign等）',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `accepter_id` bigint NULL DEFAULT NULL COMMENT '接单者ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待接单，1-进行中，2-已完成，3-已取消',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`t_id`) USING BTREE,
  INDEX `idx_publisher_id`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_accepter_id`(`accepter_id` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_task_accepter` FOREIGN KEY (`accepter_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES (1, 2, '帮忙取快递', '菜鸟驿站有个快递，帮忙取一下，取件码：1234', 'errand', 5, '菜鸟驿站', '2025-11-09 19:09:44', 19, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:41:36', NULL);
INSERT INTO `task` VALUES (2, 3, '图书馆借书', '帮忙去图书馆借一本《算法导论》，书号：TP301.6/C73', 'borrow', 8, '图书馆', '2025-11-10 19:09:44', 48, 1, NULL, '2025-11-08 19:09:44', '2025-11-11 15:47:48', NULL);
INSERT INTO `task` VALUES (3, 5, '代签到', '明天上午第一节课帮忙签到，教室：教学楼A101', 'sign', 3, '教学楼A101', '2025-11-09 19:09:44', NULL, 0, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44', NULL);
INSERT INTO `task` VALUES (4, 18, '帮忙取快递 - 顺丰快递', '我在上课不方便去拿快递，希望有人帮我取一下，地点在学校北门顺丰驿站，取货码1234', 'errand', 5, '学校北门顺丰驿站', '2025-11-09 18:00:00', 19, 2, NULL, '2025-11-08 19:40:59', '2025-11-08 19:47:05', '2025-11-08 19:47:05');
INSERT INTO `task` VALUES (5, 18, '代购奶茶 - 蜜雪冰城', '想喝蜜雪冰城的柠檬水，帮忙带一杯，谢谢！', 'purchase', 3, '学校东门蜜雪冰城', '2025-11-08 20:00:00', NULL, 0, NULL, '2025-11-08 19:41:05', '2025-11-08 19:41:05', NULL);
INSERT INTO `task` VALUES (6, 18, '帮忙打印文件', '需要打印一份文档，大概10页，图书馆打印店即可', 'other', 2, '图书馆一楼打印店', '2025-11-09 12:00:00', NULL, 0, NULL, '2025-11-08 19:41:08', '2025-11-08 19:41:08', NULL);
INSERT INTO `task` VALUES (7, 23, '测试任务 - 用于验证积分日志', '这是一个测试任务，用于验证积分日志记录功能', 'other', 10, '测试地点', '2025-11-10 18:00:00', 24, 2, NULL, '2025-11-08 20:27:10', '2025-11-08 20:27:42', '2025-11-08 20:27:42');
INSERT INTO `task` VALUES (8, 23, '测试任务2 - 用于取消', '这个任务将被取消，用于测试退还积分', 'other', 5, '测试地点', '2025-11-10 20:00:00', NULL, 3, NULL, '2025-11-08 20:27:53', '2025-11-08 20:27:55', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `u_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt）',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` int NULL DEFAULT NULL COMMENT '年级',
  `role` enum('student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'student' COMMENT '角色',
  `points` int NOT NULL DEFAULT 0 COMMENT '积分',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_verified` tinyint NOT NULL DEFAULT 0 COMMENT '是否实名认证：0-否，1-是',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`u_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_user_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '系统管理员', 'admin@campuslink.com', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, NULL, NULL, 'admin', 10000, 10, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (2, 'zhangsan', '张三', 'zhangsan@example.com', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, '2021001', NULL, '计算机科学与技术', 2021, 'student', 525, 3, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (3, 'lisi', '李四', 'lisi@example.com', '13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, '2021002', NULL, '软件工程', 2021, 'student', 330, 2, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (4, 'wangwu', '王五', 'wangwu@example.com', '13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, '2020001', NULL, '电子信息工程', 2020, 'student', 806, 5, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (5, 'zhaoliu', '赵六', 'zhaoliu@example.com', '13800000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, '2022001', NULL, '数据科学与大数据技术', 2022, 'student', 232, 1, 1, 0, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (6, 'sunqi', '孙七', 'sunqi@example.com', '13800000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, '2019001', NULL, '人工智能', 2019, 'student', 1224, 8, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (18, 'task_publisher_007', '任务发布者', 'publisher@campuslink.com', '13800010005', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 90, 1, 1, 0, NULL, '2025-11-08 19:40:34', '2025-11-08 19:40:34');
INSERT INTO `user` VALUES (19, 'task_accepter_007', '任务接单者', 'acceter@campuslink.com', '13800000302', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 105, 1, 1, 0, NULL, '2025-11-08 19:40:50', '2025-11-08 19:40:50');
INSERT INTO `user` VALUES (20, 'club_creator_001', '社团创建者001', 'creator001@campuslink.com', '13900001001', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 20:14:16', '2025-11-08 20:14:16');
INSERT INTO `user` VALUES (21, 'club_member_002', '社团成员002', 'member002@campuslink.com', '13900001002', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 20:14:28', '2025-11-08 20:14:28');
INSERT INTO `user` VALUES (22, 'club_member_003', '社团成员003', 'member003@campuslink.com', '13900001003', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 20:17:14', '2025-11-08 20:17:14');
INSERT INTO `user` VALUES (23, 'user_test_001', '新昵称', 'user001@campuslink.com', '13900000002', 'e10adc3949ba59abbe56e057f20f883e', 'https://example.com/new-avatar.jpg', '2021001001', NULL, '计算机科学与技术', 2021, 'student', 90, 1, 1, 0, NULL, '2025-11-08 20:26:31', '2025-11-08 20:26:52');
INSERT INTO `user` VALUES (24, 'user_test_002', '测试用户002', 'user002@campuslink.com', '13900000003', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 110, 1, 1, 0, NULL, '2025-11-08 20:27:23', '2025-11-08 20:27:23');
INSERT INTO `user` VALUES (25, 'message_user_001', '消息测试用户1', 'message_user1@test.com', '13900000001', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 20:58:59', '2025-11-08 20:58:59');
INSERT INTO `user` VALUES (26, 'message_user_002', '消息测试用户2', 'message_user2@test.com', '13830000002', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 20:59:12', '2025-11-08 20:59:12');
INSERT INTO `user` VALUES (27, 'report_user_001', '举报测试用户', 'report_user@test.com', '13800000010', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 21:01:07', '2025-11-08 21:01:07');
INSERT INTO `user` VALUES (28, 'report_admin_001', '举报管理员', 'report_admin@test.com', '13800000011', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'admin', 100, 1, 1, 0, NULL, '2025-11-08 21:01:19', '2025-11-08 21:01:19');
INSERT INTO `user` VALUES (29, 'notify_user_001', '通知测试用户1', 'notify001@campus.com', '13800000021', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 21:14:37', '2025-11-08 21:14:37');
INSERT INTO `user` VALUES (30, 'notify_user_002', '通知测试用户2', 'notify002@campus.com', '13800000022', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 21:14:54', '2025-11-08 21:14:54');
INSERT INTO `user` VALUES (31, 'notify_user_003', '通知测试用户3', 'notify003@campus.com', '13800000023', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-08 21:15:05', '2025-11-08 21:15:05');
INSERT INTO `user` VALUES (32, 'notify_admin', '通知管理员', 'notifyadmin@campus.com', '13800000024', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 1, 0, NULL, '2025-11-08 21:15:16', '2025-11-08 21:15:16');
INSERT INTO `user` VALUES (37, 'resource_user_001', '资源上传者1', 'resource001@campus.com', '13900000031', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 103, 1, 1, 0, NULL, '2025-11-09 13:09:31', '2025-11-09 13:09:31');
INSERT INTO `user` VALUES (38, 'resource_user_002', '资源下载者1', 'resource002@campus.com', '13900000032', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 79, 1, 1, 0, NULL, '2025-11-09 13:09:45', '2025-11-09 13:09:45');
INSERT INTO `user` VALUES (39, 'resource_user_003', '资源下载者2', 'resource003@campus.com', '13900000033', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 13:10:04', '2025-11-09 13:10:04');
INSERT INTO `user` VALUES (40, 'resource_admin', '资源管理员', 'resadmin@campus.com', '13900000034', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 1, 0, NULL, '2025-11-09 13:10:16', '2025-11-09 13:10:16');
INSERT INTO `user` VALUES (41, 'school_admin', '学校管理员', 'schooladmin@campus.com', '13900000041', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 1, 0, NULL, '2025-11-09 13:36:52', '2025-11-09 13:36:52');
INSERT INTO `user` VALUES (42, 'school_user_001', '普通用户', 'schooluser@campus.com', '13900000042', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 13:37:01', '2025-11-09 13:37:01');
INSERT INTO `user` VALUES (43, 'comment_user_001', '评论测试用户1', 'comment001@campus.com', '13900030041', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 14:05:50', '2025-11-09 14:05:50');
INSERT INTO `user` VALUES (44, 'comment_user_002', '评论测试用户2', 'comment002@campus.com', '13900020042', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 14:06:10', '2025-11-09 14:06:10');
INSERT INTO `user` VALUES (45, 'comment_user_003', '评论测试用户3', 'comment003@campus.com', '13900100043', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 14:06:24', '2025-11-09 14:06:24');
INSERT INTO `user` VALUES (46, 'config_admin', '配置管理员', 'configadmin@campus.com', '13900000051', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'admin', 100, 1, 1, 0, NULL, '2025-11-09 14:17:50', '2025-11-09 14:17:50');
INSERT INTO `user` VALUES (47, 'config_user', '普通用户', 'configuser@campus.com', '13900000052', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 1, 0, NULL, '2025-11-09 14:18:04', '2025-11-09 14:18:04');
INSERT INTO `user` VALUES (48, '2164601212', '2164601212', '2164601212@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 1, 0, '2025-11-11 15:40:31', '2025-11-10 21:03:00', '2025-11-10 21:03:00');

-- ----------------------------
-- View structure for v_hot_tags
-- ----------------------------
DROP VIEW IF EXISTS `v_hot_tags`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_hot_tags` AS select `tag`.`tag_id` AS `tag_id`,`tag`.`tag_name` AS `tag_name`,`tag`.`display_name` AS `display_name`,`tag`.`use_count` AS `use_count`,`tag`.`category` AS `category`,`tag`.`description` AS `description` from `tag` where (`tag`.`status` = 1) order by `tag`.`use_count` desc limit 20;

-- ----------------------------
-- Procedure structure for sp_increment_tag_use_count
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_increment_tag_use_count`;
delimiter ;;
CREATE PROCEDURE `sp_increment_tag_use_count`(IN p_tag_name VARCHAR(50),
  IN p_increment INT)
BEGIN
  DECLARE v_tag_id BIGINT;

  -- 查找标签ID
  SELECT `tag_id` INTO v_tag_id
  FROM `tag`
  WHERE `tag_name` = p_tag_name AND `status` = 1;

  -- 如果标签存在，增加使用次数
  IF v_tag_id IS NOT NULL THEN
    UPDATE `tag`
    SET `use_count` = `use_count` + p_increment
    WHERE `tag_id` = v_tag_id;
  END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
