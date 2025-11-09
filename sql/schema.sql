-- ============================================
-- CampusLink 数据库建表脚本
-- 版本: v1.0
-- 创建时间: 2024-01-01
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `campuslink` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `campuslink`;

-- ============================================
-- 1. 学校表 (school)
-- ============================================
DROP TABLE IF EXISTS `school`;
CREATE TABLE `school` (
  `school_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  `school_name` VARCHAR(200) NOT NULL COMMENT '学校名称',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
  `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '学校Logo',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`school_id`),
  KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学校信息表';

-- ============================================
-- 2. 用户表 (user)
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `u_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `school_id` BIGINT DEFAULT NULL COMMENT '所属学校ID',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `grade` INT DEFAULT NULL COMMENT '年级',
  `role` ENUM('student', 'teacher', 'admin') NOT NULL DEFAULT 'student' COMMENT '角色',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
  `level` INT NOT NULL DEFAULT 1 COMMENT '等级',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '是否实名认证：0-否，1-是',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`u_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_user_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 3. 资源表 (resource)
-- ============================================
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `r_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` VARCHAR(200) NOT NULL COMMENT '资源标题',
  `description` TEXT DEFAULT NULL COMMENT '资源描述',
  `uploader_id` BIGINT NOT NULL COMMENT '上传者ID',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL（OSS）',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) NOT NULL COMMENT '文件类型（pdf/doc/ppt等）',
  `category` VARCHAR(50) NOT NULL COMMENT '分类（课件/试题/笔记等）',
  `course_name` VARCHAR(100) DEFAULT NULL COMMENT '课程名称',
  `school_id` BIGINT DEFAULT NULL COMMENT '所属学校ID',
  `score` INT NOT NULL DEFAULT 0 COMMENT '所需积分',
  `downloads` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`r_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resource_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源表';

-- ============================================
-- 4. 问题表 (question)
-- ============================================
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `q_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题内容',
  `asker_id` BIGINT NOT NULL COMMENT '提问者ID',
  `category` VARCHAR(50) NOT NULL COMMENT '分类（学习/生活/技术等）',
  `tags` JSON DEFAULT NULL COMMENT '标签（JSON数组）',
  `images` JSON DEFAULT NULL COMMENT '图片URL列表（JSON数组）',
  `ai_answer` TEXT DEFAULT NULL COMMENT 'AI生成的答案',
  `ai_generated_at` DATETIME DEFAULT NULL COMMENT 'AI答案生成时间',
  `views` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数量',
  `is_solved` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已解决：0-否，1-是',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`q_id`),
  KEY `idx_asker_id` (`asker_id`),
  KEY `idx_category` (`category`),
  KEY `idx_is_solved` (`is_solved`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_question_asker` FOREIGN KEY (`asker_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- ============================================
-- 5. 回答表 (answer)
-- ============================================
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
  `a_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` BIGINT NOT NULL COMMENT '问题ID',
  `responder_id` BIGINT NOT NULL COMMENT '回答者ID',
  `content` TEXT NOT NULL COMMENT '回答内容',
  `images` JSON DEFAULT NULL COMMENT '图片URL列表（JSON数组）',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `is_accepted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被采纳：0-否，1-是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`a_id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_responder_id` (`responder_id`),
  KEY `idx_is_accepted` (`is_accepted`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`q_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_answer_responder` FOREIGN KEY (`responder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答表';

-- ============================================
-- 6. 任务表 (task)
-- ============================================
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `t_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `content` TEXT NOT NULL COMMENT '任务内容',
  `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型（errand/borrow/sign等）',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '地点',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止时间',
  `accepter_id` BIGINT DEFAULT NULL COMMENT '接单者ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待接单，1-进行中，2-已完成，3-已取消',
  `images` JSON DEFAULT NULL COMMENT '图片URL列表（JSON数组）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`t_id`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_accepter_id` (`accepter_id`),
  KEY `idx_task_type` (`task_type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_task_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_accepter` FOREIGN KEY (`accepter_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ============================================
-- 7. 私信表 (message)
-- ============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `m_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `msg_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-文件',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`m_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信表';

-- ============================================
-- 8. 社团表 (club)
-- ============================================
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club` (
  `club_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '社团ID',
  `club_name` VARCHAR(100) NOT NULL COMMENT '社团名称',
  `description` TEXT DEFAULT NULL COMMENT '社团简介',
  `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '社团Logo',
  `school_id` BIGINT NOT NULL COMMENT '所属学校ID',
  `founder_id` BIGINT NOT NULL COMMENT '创建者ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已解散，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`club_id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_founder_id` (`founder_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_club_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_founder` FOREIGN KEY (`founder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- ============================================
-- 9. 社团成员表 (club_member)
-- ============================================
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member` (
  `cm_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `club_id` BIGINT NOT NULL COMMENT '社团ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role` ENUM('member', 'admin', 'founder') NOT NULL DEFAULT 'member' COMMENT '角色',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`cm_id`),
  UNIQUE KEY `uk_club_user` (`club_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_club_member_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_club_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- ============================================
-- 10. 活动表 (activity)
-- ============================================
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `activity_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `club_id` BIGINT NOT NULL COMMENT '社团ID',
  `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
  `description` TEXT DEFAULT NULL COMMENT '活动描述',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `max_participants` INT DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` INT NOT NULL DEFAULT 0 COMMENT '当前参与人数',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '参与奖励积分',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已取消',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`activity_id`),
  KEY `idx_club_id` (`club_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  CONSTRAINT `fk_activity_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- ============================================
-- 11. 活动参与表 (activity_participant)
-- ============================================
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant` (
  `ap_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `is_signed_in` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已签到：0-否，1-是',
  `signed_in_at` DATETIME DEFAULT NULL COMMENT '签到时间',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`ap_id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_activity_participant_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`activity_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_activity_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动参与表';

-- ============================================
-- 12. 举报表 (report)
-- ============================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报者ID',
  `report_type` TINYINT NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户，4-活动',
  `target_id` BIGINT NOT NULL COMMENT '举报对象ID',
  `reason_type` TINYINT NOT NULL COMMENT '举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他',
  `description` TEXT NOT NULL COMMENT '举报详细描述',
  `evidence_images` VARCHAR(1000) DEFAULT NULL COMMENT '举报凭证图片(多张用逗号分隔)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已处理，2-已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`report_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================
-- 13. 积分记录表 (points_log)
-- ============================================
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points_change` INT NOT NULL COMMENT '积分变化（正数为增加，负数为减少）',
  `points_after` INT NOT NULL COMMENT '变化后的积分',
  `reason` VARCHAR(200) NOT NULL COMMENT '变化原因',
  `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联对象类型',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联对象ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_points_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- ============================================
-- 14. 下载记录表 (download_log)
-- ============================================
DROP TABLE IF EXISTS `download_log`;
CREATE TABLE `download_log` (
  `dl_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '下载记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `points_cost` INT NOT NULL DEFAULT 0 COMMENT '消耗积分',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  PRIMARY KEY (`dl_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_download_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_download_log_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下载记录表';

-- ============================================
-- 15. 评论表 (comment)
-- ============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `target_type` VARCHAR(50) NOT NULL COMMENT '评论对象类型（resource/question/answer等）',
  `target_id` BIGINT NOT NULL COMMENT '评论对象ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（用于回复）',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 16. 通知表 (notification)
-- ============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `notification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `notify_type` VARCHAR(50) NOT NULL COMMENT '通知类型（system/answer/task/activity等）',
  `related_type` VARCHAR(50) DEFAULT NULL COMMENT '关联对象类型',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联对象ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ============================================
-- 17. 系统配置表 (system_config)
-- ============================================
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

