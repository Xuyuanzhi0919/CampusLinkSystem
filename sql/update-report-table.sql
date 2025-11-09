-- ============================================
-- 更新举报表结构
-- 执行时间: 2025-11-08
-- 说明: 修改report表结构以匹配Java实体类
-- ============================================

USE `campuslink`;

-- 删除旧的report表
DROP TABLE IF EXISTS `report`;

-- 创建新的report表
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
