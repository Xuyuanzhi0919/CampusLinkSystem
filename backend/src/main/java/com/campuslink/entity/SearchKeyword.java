package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索关键词实体类
 * 用于记录和统计用户搜索行为，支持热词排行功能
 */
@Data
@TableName("search_keyword")
public class SearchKeyword {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 总搜索次数
     */
    @TableField("search_count")
    private Integer searchCount;

    /**
     * 今日搜索次数
     */
    @TableField("daily_count")
    private Integer dailyCount;

    /**
     * 本周搜索次数
     */
    @TableField("weekly_count")
    private Integer weeklyCount;

    /**
     * 上次排名（用于计算趋势）
     */
    @TableField("last_rank")
    private Integer lastRank;

    /**
     * 当前排名
     */
    @TableField("current_rank")
    private Integer currentRank;

    /**
     * 首次搜索时间
     */
    @TableField("first_search_at")
    private LocalDateTime firstSearchAt;

    /**
     * 最后搜索时间
     */
    @TableField("last_search_at")
    private LocalDateTime lastSearchAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
