package com.campuslink.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录响应
 */
@Data
public class PointsLogResponse {
    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 积分变化
     */
    private Integer pointsChange;

    /**
     * 变化后的积分
     */
    private Integer pointsAfter;

    /**
     * 变化原因
     */
    private String reason;

    /**
     * 关联对象类型
     */
    private String relatedType;

    /**
     * 关联对象ID
     */
    private Long relatedId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
