-- ============================================================
-- 定时公告功能迁移脚本
-- 对应后端：entity/ScheduledNotice.java
-- 执行方式：mysql -u root -p campuslink < sql/add_scheduled_notice.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `scheduled_notice` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT        COMMENT '主键',
  `title`        VARCHAR(100) NOT NULL                       COMMENT '公告标题',
  `content`      VARCHAR(500) NOT NULL                       COMMENT '公告内容',
  `notify_type`  VARCHAR(20)  NOT NULL                       COMMENT '通知类型：announcement/system/warning',
  `target_type`  VARCHAR(10)  NOT NULL                       COMMENT '发送对象类型：all/single/role',
  `target_value` VARCHAR(50)  DEFAULT NULL                   COMMENT 'userId（single 模式）或 role 值（role 模式），全体广播为 NULL',
  `scheduled_at` DATETIME     NOT NULL                       COMMENT '计划发送时间',
  `status`       TINYINT      NOT NULL DEFAULT 0             COMMENT '状态：0=待发送 1=已发送 2=已取消/失败',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_status_scheduled_at` (`status`, `scheduled_at`)  COMMENT '调度器查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时公告队列';
