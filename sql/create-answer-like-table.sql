-- 答案点赞表
-- 用于记录用户对答案的点赞关系

CREATE TABLE IF NOT EXISTS `answer_like` (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `answer_id` bigint NOT NULL COMMENT '答案ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_user_answer` (`user_id` ASC, `answer_id` ASC) USING BTREE COMMENT '用户答案唯一索引，防止重复点赞',
  INDEX `idx_answer_id` (`answer_id` ASC) USING BTREE COMMENT '答案ID索引',
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_created_at` (`created_at` ASC) USING BTREE COMMENT '创建时间索引',
  CONSTRAINT `fk_answer_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_like_answer` FOREIGN KEY (`answer_id`) REFERENCES `answer` (`a_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '答案点赞表' ROW_FORMAT = Dynamic;
