-- 添加社团新字段迁移脚本
-- 执行日期: 2024-12-17
-- 描述: 添加 is_official 和 category 字段到 club 表

USE campuslink;

-- 1. 添加 is_official 字段
ALTER TABLE `club`
ADD COLUMN `is_official` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否官方/校级社团：0-否，1-是' AFTER `status`;

-- 2. 添加 category 字段
ALTER TABLE `club`
ADD COLUMN `category` VARCHAR(20) DEFAULT '兴趣' COMMENT '社团分类: 技术/学习/体育/艺术/公益/兴趣' AFTER `is_official`;

-- 创建索引以优化查询
CREATE INDEX `idx_is_official` ON `club`(`is_official`, `status`);
CREATE INDEX `idx_category` ON `club`(`category`, `status`);

-- 示例数据: 将前2个社团设置为官方社团(用于测试)
-- UPDATE `club` SET `is_official` = 1 WHERE `club_id` IN (1, 2);

-- 示例数据: 根据社团名称设置分类(可选)
-- UPDATE `club` SET `category` = '技术' WHERE `club_name` LIKE '%计算机%' OR `club_name` LIKE '%编程%';
-- UPDATE `club` SET `category` = '学习' WHERE `club_name` LIKE '%学习%' OR `club_name` LIKE '%科学%';
-- UPDATE `club` SET `category` = '体育' WHERE `club_name` LIKE '%体育%' OR `club_name` LIKE '%篮球%' OR `club_name` LIKE '%足球%';
-- UPDATE `club` SET `category` = '艺术' WHERE `club_name` LIKE '%音乐%' OR `club_name` LIKE '%美术%' OR `club_name` LIKE '%艺术%';
-- UPDATE `club` SET `category` = '公益' WHERE `club_name` LIKE '%志愿%' OR `club_name` LIKE '%公益%';

-- 验证字段是否添加成功
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM
    INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_SCHEMA = 'campuslink'
    AND TABLE_NAME = 'club'
    AND COLUMN_NAME IN ('is_official', 'category')
ORDER BY ORDINAL_POSITION;
