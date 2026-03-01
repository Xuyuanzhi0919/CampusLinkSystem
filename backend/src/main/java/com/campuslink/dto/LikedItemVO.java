package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 获赞内容项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获赞内容项")
public class LikedItemVO {

    @Schema(description = "内容类型：resource / answer")
    private String type;

    @Schema(description = "内容ID（resource.r_id 或 answer.a_id）")
    private Long targetId;

    @Schema(description = "标题（资源标题 或 回答内容摘要）")
    private String title;

    @Schema(description = "关联问题ID（type=answer 时有值，用于跳转问题详情）")
    private Long questionId;

    @Schema(description = "关联问题标题（type=answer 时有值）")
    private String questionTitle;

    @Schema(description = "获赞数")
    private Integer likes;

    @Schema(description = "内容创建时间")
    private LocalDateTime createdAt;
}
