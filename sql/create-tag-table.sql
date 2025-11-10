-- ===================================================================
-- CampusLink - 热门标签表创建脚本
-- 功能：存储平台所有标签及其使用统计
-- 作者：Claude Code
-- 日期：2025-01-10
-- ===================================================================

USE campuslink;

-- 1. 创建标签表 (tag)
CREATE TABLE IF NOT EXISTS `tag` (
  `tag_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID（主键）',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称（不含#号）',
  `display_name` VARCHAR(50) NOT NULL COMMENT '显示名称（含#号，如：#考研资料）',
  `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数（热度）',
  `category` VARCHAR(20) NULL COMMENT '标签分类：学习/生活/技术/娱乐等',
  `description` VARCHAR(200) NULL COMMENT '标签描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`),
  KEY `idx_use_count` (`use_count` DESC),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 2. 创建标签关联表 (tag_relation)
-- 用于记录标签与具体内容（问题、资源等）的关联关系
CREATE TABLE IF NOT EXISTS `tag_relation` (
  `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键）',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '关联对象类型：question/resource/task',
  `target_id` BIGINT NOT NULL COMMENT '关联对象ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_tag_target` (`tag_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签关联表';

-- 3. 初始化热门标签数据（基于前端现有标签）
INSERT INTO `tag` (`tag_name`, `display_name`, `use_count`, `category`, `description`, `status`) VALUES
('考研资料', '#考研资料', 120, '学习', '考研相关的资料和经验分享', 1),
('学习打卡', '#学习打卡', 95, '学习', '记录和分享学习进度', 1),
('AI问答', '#AI问答', 88, '技术', 'AI和人工智能相关问答', 1),
('Python基础', '#Python基础', 76, '技术', 'Python编程基础知识', 1),
('数据结构', '#数据结构', 82, '学习', '数据结构与算法相关内容', 1),
('英语四六级', '#英语四六级', 110, '学习', '英语四六级考试资料和经验', 1),
('算法刷题', '#算法刷题', 68, '技术', '算法题目和解题思路', 1),
('前端开发', '#前端开发', 72, '技术', '前端技术和开发经验', 1),
('Java学习', '#Java学习', 64, '技术', 'Java编程学习资料', 1),
('求职面试', '#求职面试', 58, '生活', '求职和面试经验分享', 1),
('实习经验', '#实习经验', 52, '生活', '实习相关经验分享', 1),
('校园生活', '#校园生活', 90, '生活', '校园日常生活分享', 1);

-- 4. 创建视图：热门标签TOP20（方便查询）
CREATE OR REPLACE VIEW `v_hot_tags` AS
SELECT
  `tag_id`,
  `tag_name`,
  `display_name`,
  `use_count`,
  `category`,
  `description`
FROM `tag`
WHERE `status` = 1
ORDER BY `use_count` DESC
LIMIT 20;

-- 5. 创建存储过程：增加标签使用次数
DELIMITER $$

CREATE PROCEDURE `sp_increment_tag_use_count`(
  IN p_tag_name VARCHAR(50),
  IN p_increment INT
)
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
END$$

DELIMITER ;

-- 6. 创建定时清理过期标签的事件（可选）
-- 注意：需要开启事件调度器 SET GLOBAL event_scheduler = ON;
/*
CREATE EVENT IF NOT EXISTS `event_clean_unused_tags`
ON SCHEDULE EVERY 1 MONTH
STARTS CURRENT_TIMESTAMP
DO
  -- 删除3个月内使用次数为0的标签（非系统预设标签）
  DELETE FROM `tag`
  WHERE `use_count` = 0
    AND `created_at` < DATE_SUB(NOW(), INTERVAL 3 MONTH)
    AND `tag_id` > 100; -- 保护前100个系统预设标签
*/

-- ===================================================================
-- 说明：
-- 1. tag 表存储所有标签，use_count 记录热度
-- 2. tag_relation 表记录标签与问题/资源的关联关系
-- 3. v_hot_tags 视图提供快速查询热门标签TOP20
-- 4. sp_increment_tag_use_count 存储过程用于增加标签使用次数
-- 5. 初始数据基于前端现有的8个标签 + 4个扩展标签
-- ===================================================================
