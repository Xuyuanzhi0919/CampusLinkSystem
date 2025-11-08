package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录实体
 */
@Data
@TableName("points_log")
public class PointsLog {
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 积分变化（正数为增加，负数为减少）
     */
    @TableField("points_change")
    private Integer pointsChange;

    /**
     * 变化后的积分
     */
    @TableField("points_after")
    private Integer pointsAfter;

    /**
     * 变化原因
     */
    private String reason;

    /**
     * 关联对象类型
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 关联对象ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
