package com.campuslink.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 精选问题响应DTO
 * 用于首页推荐位展示的优质问题
 *
 * @author CampusLink Team
 * @since 2025-01-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "精选问题响应DTO")
public class FeaturedQuestionResponse {

    @Schema(description = "问题ID")
    private Long qid;

    @Schema(description = "问题标题")
    private String title;

    @Schema(description = "提问者用户名")
    private String username;

    @Schema(description = "提问者头像URL")
    private String avatar;

    @Schema(description = "问题分类")
    private String category;

    @Schema(description = "回答数量")
    private Integer answerCount;

    @Schema(description = "浏览次数")
    private Integer views;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "质量分数（用于排序，不对外暴露）", hidden = true)
    private Double qualityScore;
}
