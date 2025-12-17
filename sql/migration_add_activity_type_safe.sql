-- 活动广场优化：支持多种活动类型（安全版本）
-- 执行时间：2025-12-17
-- 描述：添加 activity_type 和 organizer_name 字段，支持社团活动、校园活动、官方活动
-- 特性：自动检测字段是否存在，避免重复执行错误

USE campuslink;

-- 1. 安全添加 activity_type 字段（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'activity'
  AND COLUMN_NAME = 'activity_type';

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `activity` ADD COLUMN `activity_type` VARCHAR(20) NOT NULL DEFAULT ''club'' COMMENT ''活动类型: club-社团活动, campus-校园活动, official-官方活动'' AFTER `club_id`',
  'SELECT ''activity_type 字段已存在，跳过创建'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 安全修改 club_id 为可空
SET @sql = 'ALTER TABLE `activity` MODIFY COLUMN `club_id` BIGINT NULL COMMENT ''社团ID（社团活动必填，其他类型可为空）''';
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 安全添加 organizer_name 字段（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'activity'
  AND COLUMN_NAME = 'organizer_name';

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE `activity` ADD COLUMN `organizer_name` VARCHAR(50) DEFAULT NULL COMMENT ''组织者名称（非社团活动时使用，如"校学生会"、"教务处"）'' AFTER `activity_type`',
  'SELECT ''organizer_name 字段已存在，跳过创建'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 安全添加索引（如果不存在）
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'activity'
  AND INDEX_NAME = 'idx_activity_type';

SET @sql = IF(@index_exists = 0,
  'CREATE INDEX `idx_activity_type` ON `activity`(`activity_type`, `status`, `start_time`)',
  'SELECT ''索引 idx_activity_type 已存在，跳过创建'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'activity'
  AND INDEX_NAME = 'idx_organizer';

SET @sql = IF(@index_exists = 0,
  'CREATE INDEX `idx_organizer` ON `activity`(`organizer_name`)',
  'SELECT ''索引 idx_organizer 已存在，跳过创建'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 更新现有数据：将所有现有活动标记为社团活动
UPDATE `activity`
SET `activity_type` = 'club'
WHERE `activity_type` IS NULL OR `activity_type` = '' OR `activity_type` = 'club';

-- 6. 删除旧的测试数据（如果存在）
DELETE FROM `activity`
WHERE `organizer_name` IN ('就业指导中心', '校党委宣传部', '校医院', '教务处');

-- 7. 插入测试数据：校园活动和官方活动示例
INSERT INTO `activity` (
  `title`,
  `club_id`,
  `activity_type`,
  `organizer_name`,
  `description`,
  `location`,
  `start_time`,
  `end_time`,
  `max_participants`,
  `current_participants`,
  `status`,
  `cover_image`,
  `created_at`,
  `updated_at`
) VALUES
(
  '2025春季校园招聘会',
  NULL,
  'campus',
  '就业指导中心',
  '特邀500+知名企业现场招聘，提供实习及全职岗位。涵盖互联网、金融、制造等多个行业，欢迎各专业同学参加。',
  '体育馆一层',
  '2025-03-15 09:00:00',
  '2025-03-15 17:00:00',
  2000,
  356,
  0,
  'https://via.placeholder.com/800x450/4F46E5/FFFFFF?text=Campus+Job+Fair',
  NOW(),
  NOW()
),
(
  '校庆70周年文艺晚会',
  NULL,
  'official',
  '校党委宣传部',
  '庆祝建校70周年主题晚会，包含歌舞、话剧、器乐演奏等精彩节目。特邀知名校友回校演出，共庆母校华诞。',
  '大礼堂',
  '2025-04-20 19:00:00',
  '2025-04-20 21:30:00',
  1500,
  892,
  0,
  'https://via.placeholder.com/800x450/EC4899/FFFFFF?text=70th+Anniversary',
  NOW(),
  NOW()
),
(
  '急救知识专题讲座',
  NULL,
  'campus',
  '校医院',
  '邀请市人民医院急救科专家讲解心肺复苏、海姆立克急救法等实用技能，现场演示+互动体验。',
  '图书馆报告厅',
  '2025-03-25 14:00:00',
  '2025-03-25 16:00:00',
  300,
  127,
  0,
  'https://via.placeholder.com/800x450/10B981/FFFFFF?text=First+Aid+Lecture',
  NOW(),
  NOW()
),
(
  '考研经验分享会',
  NULL,
  'official',
  '教务处',
  '邀请2024届成功上岸的学长学姐分享备考经验，涵盖择校、复习规划、心态调整等话题，并提供一对一答疑。',
  '教学楼A101',
  '2025-03-10 18:30:00',
  '2025-03-10 20:30:00',
  500,
  423,
  0,
  'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Postgraduate+Talk',
  NOW(),
  NOW()
);

-- 8. 验证数据
SELECT
  activity_id,
  title,
  activity_type,
  organizer_name,
  club_id,
  CASE
    WHEN club_id IS NOT NULL THEN CONCAT('社团ID:', club_id)
    ELSE '非社团活动'
  END AS club_info
FROM `activity`
ORDER BY `created_at` DESC
LIMIT 10;

-- 9. 统计各类型活动数量
SELECT
  activity_type,
  COUNT(*) AS count,
  GROUP_CONCAT(DISTINCT organizer_name) AS organizers
FROM `activity`
GROUP BY activity_type;

-- 迁移完成提示
SELECT '✅ 活动类型字段迁移完成！已添加 activity_type、organizer_name 字段，并插入 4 条测试数据' AS message;
