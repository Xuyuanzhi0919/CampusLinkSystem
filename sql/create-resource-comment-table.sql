-- 资源评论表
-- 用于记录用户对资源的评论和回复

DROP TABLE IF EXISTS `resource_comment`;
CREATE TABLE `resource_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `user_id` bigint NOT NULL COMMENT '评论者用户ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（NULL表示一级评论，非NULL表示回复）',
  `content` text NOT NULL COMMENT '评论内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除标志（0=正常，1=已删除）',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_resource_id` (`resource_id` ASC) USING BTREE COMMENT '资源ID索引',
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_parent_id` (`parent_id` ASC) USING BTREE COMMENT '父评论ID索引',
  INDEX `idx_created_at` (`created_at` DESC) USING BTREE COMMENT '创建时间索引',
  CONSTRAINT `fk_resource_comment_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `resource_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源评论表' ROW_FORMAT = Dynamic;
