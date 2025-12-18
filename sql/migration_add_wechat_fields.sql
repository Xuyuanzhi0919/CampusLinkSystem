-- ========================================
-- 数据库迁移：为用户表添加微信小程序登录字段
-- 用途：支持微信小程序一键登录和账号绑定
-- 日期：2025-12-18
-- ========================================

USE campuslink;

-- 1. 添加微信 OpenID 字段（微信小程序用户唯一标识）
ALTER TABLE `user`
ADD COLUMN `wechat_openid` VARCHAR(100) NULL DEFAULT NULL COMMENT '微信OpenID（小程序用户唯一标识）'
AFTER `password_hash`;

-- 2. 添加微信 UnionID 字段（同一微信开放平台下的唯一标识）
ALTER TABLE `user`
ADD COLUMN `wechat_unionid` VARCHAR(100) NULL DEFAULT NULL COMMENT '微信UnionID（开放平台统一标识）'
AFTER `wechat_openid`;

-- 3. 为 wechat_openid 添加唯一索引（防止重复绑定）
ALTER TABLE `user`
ADD UNIQUE INDEX `uk_wechat_openid`(`wechat_openid` ASC) USING BTREE COMMENT '微信OpenID唯一索引';

-- 4. 为 wechat_unionid 添加索引（加速查询）
ALTER TABLE `user`
ADD INDEX `idx_wechat_unionid`(`wechat_unionid` ASC) USING BTREE COMMENT '微信UnionID索引';

-- 5. 修改 username 唯一约束为条件唯一（允许微信用户 username 为 NULL）
-- 注意：MySQL 5.7+ 的唯一索引会将 NULL 视为不同值，因此多个 NULL 不会冲突
-- 如果需要兼容旧版本，可以考虑使用默认值而非 NULL

-- 6. 修改 password_hash 字段为可空（微信登录用户无需密码）
ALTER TABLE `user`
MODIFY COLUMN `password_hash` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码哈希（BCrypt），微信登录用户可为空';

-- ========================================
-- 验证迁移结果
-- ========================================
-- 查看 user 表结构
DESC `user`;

-- 查看索引
SHOW INDEX FROM `user`;

-- ========================================
-- 回滚脚本（如需回滚，请执行以下语句）
-- ========================================
/*
ALTER TABLE `user` DROP INDEX `uk_wechat_openid`;
ALTER TABLE `user` DROP INDEX `idx_wechat_unionid`;
ALTER TABLE `user` DROP COLUMN `wechat_unionid`;
ALTER TABLE `user` DROP COLUMN `wechat_openid`;
ALTER TABLE `user` MODIFY COLUMN `password_hash` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt）';
*/
