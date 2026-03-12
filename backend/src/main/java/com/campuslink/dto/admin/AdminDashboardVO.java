package com.campuslink.dto.admin;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理员仪表板统计数据
 */
@Data
public class AdminDashboardVO {
    // 核心指标
    private Long totalUsers;
    private Long totalResources;
    private Long totalQuestions;
    private Long totalTasks;
    private Long totalActivities;

    // 今日新增
    private Long todayNewUsers;
    private Long todayNewResources;
    private Long todayNewQuestions;
    private Long todayNewTasks;

    // 待处理
    private Long pendingResources;   // 待审核资源
    private Long pendingReports;     // 待处理举报
    private Long bannedUsers;        // 封禁用户数

    // 近7天用户注册趋势：[{"date": "03-06", "count": 12}, ...]
    private List<Map<String, Object>> userTrend;

    // 近7天内容发布趋势
    private List<Map<String, Object>> contentTrend;
}
