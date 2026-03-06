package com.campuslink.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 标签名称（不含#号）
     */
    private String tagName;

    /**
     * 显示名称（含#号）
     */
    private String displayName;

    /**
     * 使用次数（热度）
     */
    private Integer useCount;

    /**
     * 标签分类
     */
    private String category;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 热度趋势：up=上升, down=下降, stable=稳定
     */
    private String trend;

    /**
     * 趋势变化百分比（正数上升，负数下降）
     */
    private Integer trendPercent;

    /**
     * 关联的问题数量
     */
    private Integer questionCount;

    /**
     * 关联的资源数量
     */
    private Integer resourceCount;
}
