package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录视图对象
 */
@Data
@Schema(description = "积分记录")
public class PointsLogVO {

    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    private Long logId;

    /**
     * 积分变化（正数为增加，负数为减少）
     */
    @Schema(description = "积分变化")
    private Integer pointsChange;

    /**
     * 变化后的积分
     */
    @Schema(description = "变化后的积分")
    private Integer pointsAfter;

    /**
     * 变化原因
     */
    @Schema(description = "变化原因")
    private String reason;

    /**
     * 关联对象类型
     */
    @Schema(description = "关联对象类型")
    private String relatedType;

    /**
     * 关联对象ID
     */
    @Schema(description = "关联对象ID")
    private Long relatedId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
