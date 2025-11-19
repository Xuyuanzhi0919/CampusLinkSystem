package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏项")
public class FavoriteItemVO {
    /**
     * 收藏ID
     */
    @Schema(description = "收藏ID")
    private Long favoriteId;

    /**
     * 收藏对象类型 (resource/question/task)
     */
    @Schema(description = "收藏对象类型")
    private String targetType;

    /**
     * 收藏对象ID
     */
    @Schema(description = "收藏对象ID")
    private Long targetId;

    /**
     * 收藏对象标题
     */
    @Schema(description = "对象标题")
    private String title;

    /**
     * 收藏对象描述/内容摘要
     */
    @Schema(description = "对象描述")
    private String description;

    /**
     * 对象创建者ID
     */
    @Schema(description = "创建者ID")
    private Long creatorId;

    /**
     * 对象创建者名称
     */
    @Schema(description = "创建者名称")
    private String creatorName;

    /**
     * 浏览数/下载数
     */
    @Schema(description = "浏览数")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Schema(description = "点赞数")
    private Integer likeCount;

    /**
     * 收藏时间
     */
    @Schema(description = "收藏时间")
    private LocalDateTime createdAt;

    /**
     * 对象创建时间
     */
    @Schema(description = "对象创建时间")
    private LocalDateTime targetCreatedAt;
}
