-- 资源点赞表
-- 用于记录用户对资源的点赞关系

DROP TABLE IF EXISTS `resource_like`;
CREATE TABLE `resource_like` (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource` (`user_id` ASC, `resource_id` ASC) USING BTREE COMMENT '用户资源唯一索引，防止重复点赞',
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_resource_id` (`resource_id` ASC) USING BTREE COMMENT '资源ID索引',
  CONSTRAINT `fk_resource_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_like_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源点赞表' ROW_FORMAT = Dynamic;
