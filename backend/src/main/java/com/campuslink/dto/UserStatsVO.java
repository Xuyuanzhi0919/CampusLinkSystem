package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计数据")
public class UserStatsVO {

    @Schema(description = "上传资源数")
    private Integer resourceCount;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "提问数")
    private Integer questionCount;

    @Schema(description = "回答数")
    private Integer answerCount;

    @Schema(description = "被采纳回答数")
    private Integer acceptedAnswerCount;

    @Schema(description = "发布任务数")
    private Integer taskPublishCount;

    @Schema(description = "接取任务数（进行中+已完成）")
    private Integer taskAcceptedCount;

    @Schema(description = "完成任务数")
    private Integer taskCompleteCount;

    @Schema(description = "收藏数")
    private Integer favoriteCount;

    @Schema(description = "获赞数")
    private Integer likeCount;

    @Schema(description = "连续签到天数")
    private Integer checkInDays;
}
