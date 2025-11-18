-- 资源评分表
CREATE TABLE IF NOT EXISTS `resource_rating` (
    `rating_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评分ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `user_id` BIGINT NOT NULL COMMENT '评分用户ID',
    `rating` INT NOT NULL CHECK (`rating` >= 1 AND `rating` <= 5) COMMENT '评分(1-5)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`rating_id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`), -- 每个用户对同一资源只能评分一次
    KEY `idx_resource_id` (`resource_id`), -- 按资源查询评分
    KEY `idx_user_id` (`user_id`) -- 按用户查询评分
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源评分表';
