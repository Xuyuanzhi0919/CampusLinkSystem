package com.campuslink.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日活跃统计响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatsResponse {
    /**
     * 今日活跃用户数
     */
    private Integer activeUsers;

    /**
     * 今日新增问答数
     */
    private Integer newQuestions;

    /**
     * 今日资源上传数
     */
    private Integer newResources;

    /**
     * 今日新增任务数
     */
    private Integer newTasks;

    /**
     * 今日活动参与数
     */
    private Integer activityParticipants;
}
