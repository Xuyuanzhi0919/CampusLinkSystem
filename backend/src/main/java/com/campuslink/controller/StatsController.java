package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.stats.TodayStatsResponse;
import com.campuslink.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 * 提供平台统计数据接口（无需登录）
 */
@Tag(name = "统计模块", description = "今日活跃、平台统计等公开数据接口")
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    /**
     * 获取平台数据统计
     * GET /stats/today
     * 无需登录，游客可访问
     */
    @Operation(summary = "获取平台数据统计", description = "返回注册用户数、今日新增问答、今日资源上传等数据，10分钟缓存")
    @GetMapping("/today")
    public Result<TodayStatsResponse> getTodayStats() {
        TodayStatsResponse stats = statsService.getTodayStats();
        return Result.success(stats);
    }

    /**
     * 获取平台总体统计（可选）
     * GET /stats/total
     * 无需登录，游客可访问
     */
    @Operation(summary = "获取平台总体统计", description = "返回平台用户总数、问题总数、资源总数等数据")
    @GetMapping("/total")
    public Result<TodayStatsResponse> getTotalStats() {
        TodayStatsResponse stats = statsService.getTotalStats();
        return Result.success(stats);
    }
}
