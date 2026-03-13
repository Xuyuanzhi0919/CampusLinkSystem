package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时公告实体
 *
 * 建表 SQL（需手动执行）：
 * CREATE TABLE scheduled_notice (
 *   id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   title       VARCHAR(100) NOT NULL,
 *   content     VARCHAR(500) NOT NULL,
 *   notify_type VARCHAR(20)  NOT NULL,
 *   target_type VARCHAR(10)  NOT NULL COMMENT 'all/single/role',
 *   target_value VARCHAR(50)  DEFAULT NULL COMMENT 'userId 或 role 值',
 *   scheduled_at DATETIME    NOT NULL,
 *   status      TINYINT      DEFAULT 0 COMMENT '0-待发送 1-已发送 2-已取消',
 *   created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 */
@Data
@TableName("scheduled_notice")
public class ScheduledNotice {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    @TableField("notify_type")
    private String notifyType;

    /** all / single / role */
    @TableField("target_type")
    private String targetType;

    /** userId（single 模式）或 role 值（role 模式），全体广播为 null */
    @TableField("target_value")
    private String targetValue;

    @TableField("scheduled_at")
    private LocalDateTime scheduledAt;

    /** 0-待发送  1-已发送  2-已取消 */
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
