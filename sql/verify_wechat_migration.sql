-- ========================================
-- 验证微信字段迁移状态
-- ========================================

USE campuslink;

-- 查看 user 表结构（筛选微信相关字段）
SELECT
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '类型',
    IS_NULLABLE AS '可空',
    COLUMN_KEY AS '索引',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'user'
  AND (COLUMN_NAME LIKE '%wechat%' OR COLUMN_NAME = 'password_hash')
ORDER BY ORDINAL_POSITION;

-- 查看微信相关索引
SELECT
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '字段名',
    NON_UNIQUE AS '非唯一',
    INDEX_TYPE AS '索引类型'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'campuslink'
  AND TABLE_NAME = 'user'
  AND (INDEX_NAME LIKE '%wechat%')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 检查是否有微信用户
SELECT
    COUNT(*) AS '微信用户数量',
    COUNT(DISTINCT wechat_openid) AS '唯一OpenID数量'
FROM user
WHERE wechat_openid IS NOT NULL;

-- 显示几条微信用户示例（如果有）
SELECT
    u_id AS '用户ID',
    username AS '用户名',
    nickname AS '昵称',
    SUBSTRING(wechat_openid, 1, 10) AS 'OpenID前缀',
    points AS '积分',
    created_at AS '创建时间'
FROM user
WHERE wechat_openid IS NOT NULL
ORDER BY created_at DESC
LIMIT 5;
