-- 任务流程增强 SQL 迁移脚本
-- 执行前请先备份数据库!

USE `campuslink`;

-- 1. 为 task 表添加新字段
ALTER TABLE `task`
ADD COLUMN `result_description` TEXT NULL COMMENT '执行者提交的结果描述' AFTER `completed_at`,
ADD COLUMN `result_attachments` JSON NULL COMMENT '结果附件URL列表' AFTER `result_description`,
ADD COLUMN `submitted_at` DATETIME NULL COMMENT '提交结果时间' AFTER `result_attachments`,
ADD COLUMN `rejection_reason` VARCHAR(500) NULL COMMENT '发布者拒绝原因' AFTER `submitted_at`,
ADD COLUMN `accepted_at` DATETIME NULL COMMENT '接单时间' AFTER `rejection_reason`,
ADD COLUMN `started_at` DATETIME NULL COMMENT '开始执行时间' AFTER `accepted_at`;

-- 2. 更新现有数据的状态说明
-- 保持现有状态值兼容性:
-- 0 - 待接单 (ACTIVE)
-- 1 - 已接取 (ACCEPTED)
-- 2 - 进行中 (IN_PROGRESS) - 需要将原来的"进行中"数据迁移
-- 3 - 待确认 (SUBMITTED) - 需要将原来的"已取消"数据迁移到 5
-- 4 - 已完成 (COMPLETED) - 需要将原来的"已完成"数据迁移
-- 5 - 已取消 (CANCELLED)
-- 6 - 已超时 (EXPIRED)

-- 2.1 备份现有数据
CREATE TABLE IF NOT EXISTS `task_backup_20251111` AS SELECT * FROM `task`;

-- 2.2 迁移现有状态数据
-- 将原来的状态1(进行中)保持为1(已接取),后续可手动调整为2(进行中)
-- 将原来的状态2(已完成)改为4(已完成)
UPDATE `task` SET `status` = 4 WHERE `status` = 2;

-- 将原来的状态3(已取消)改为5(已取消)
UPDATE `task` SET `status` = 5 WHERE `status` = 3;

-- 3. 创建任务操作日志表
CREATE TABLE IF NOT EXISTS `task_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型: publish/accept/start/submit/confirm/reject/cancel',
  `old_status` INT NULL COMMENT '操作前状态',
  `new_status` INT NOT NULL COMMENT '操作后状态',
  `remark` TEXT NULL COMMENT '备注说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_task_log_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`t_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务操作日志表';

-- 4. 创建任务评价表
CREATE TABLE IF NOT EXISTS `task_rating` (
  `rating_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `rater_id` BIGINT NOT NULL COMMENT '评价者ID',
  `rated_id` BIGINT NOT NULL COMMENT '被评价者ID',
  `rating` INT NOT NULL COMMENT '评分(1-5)',
  `comment` VARCHAR(500) NULL COMMENT '评价内容',
  `tags` JSON NULL COMMENT '评价标签(如: ["准时", "高效", "友好"])',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `uk_task_rater` (`task_id`, `rater_id`),
  KEY `idx_rated_id` (`rated_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_task_rating_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`t_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_rating_rater` FOREIGN KEY (`rater_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_task_rating_rated` FOREIGN KEY (`rated_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_rating_value` CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务评价表';

-- 5. 扩展 message 表以支持任务聊天
ALTER TABLE `message`
ADD COLUMN `related_type` VARCHAR(50) NULL COMMENT '关联类型: task/resource/question' AFTER `created_at`,
ADD COLUMN `related_id` BIGINT NULL COMMENT '关联对象ID' AFTER `related_type`,
ADD KEY `idx_related` (`related_type`, `related_id`);

-- 6. 为用户表添加信用分字段(如果还没有)
ALTER TABLE `user`
ADD COLUMN `credit_score` DECIMAL(3,1) DEFAULT 5.0 COMMENT '信用评分(0.0-5.0)' AFTER `level`,
ADD COLUMN `rating_count` INT DEFAULT 0 COMMENT '被评价次数' AFTER `credit_score`;

-- 7. 创建索引优化查询
ALTER TABLE `task`
ADD KEY `idx_status_deadline` (`status`, `deadline`),
ADD KEY `idx_accepter_status` (`accepter_id`, `status`);

-- 8. 验证数据
SELECT
    '数据迁移完成' AS message,
    COUNT(*) AS total_tasks,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS active_tasks,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS accepted_tasks,
    SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS completed_tasks,
    SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) AS cancelled_tasks
FROM `task`;
