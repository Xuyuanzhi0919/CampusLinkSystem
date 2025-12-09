package com.campuslink.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热词排行响应 DTO
 * 用于前端展示热门搜索词及其趋势
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotKeywordResponse {
    /**
     * 热词ID
     */
    private Long id;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    /**
     * 标签类型
     * new - 新上榜（24小时内首次进入榜单）
     * hot - 热门（搜索量持续高）
     * explode - 爆款（搜索量暴涨）
     */
    private String tag;

    /**
     * 趋势方向
     * up - 排名上升
     * down - 排名下降
     * stable - 排名稳定
     * new - 新上榜
     */
    private String trend;

    /**
     * 趋势值（排名变化幅度）
     * 正数表示上升位数，负数表示下降位数
     */
    private Integer trendValue;
}
