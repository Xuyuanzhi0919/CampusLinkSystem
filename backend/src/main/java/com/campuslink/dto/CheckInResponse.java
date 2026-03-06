package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 签到响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到响应")
public class CheckInResponse {

    /**
     * 是否签到成功
     */
    @Schema(description = "是否签到成功")
    private Boolean success;

    /**
     * 提示信息
     */
    @Schema(description = "提示信息")
    private String message;

    /**
     * 获得的积分
     */
    @Schema(description = "获得的积分")
    private Integer pointsEarned;

    /**
     * 签到后的总积分
     */
    @Schema(description = "签到后的总积分")
    private Integer totalPoints;

    /**
     * 连续签到天数
     */
    @Schema(description = "连续签到天数")
    private Integer consecutiveDays;

    /**
     * 签到时间
     */
    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
}
