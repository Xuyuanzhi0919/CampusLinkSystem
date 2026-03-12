package com.campuslink.controller.admin;

import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminDashboardVO;
import com.campuslink.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员仪表板 & 统计接口
 */
@Tag(name = "管理端 - 仪表板统计")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminService adminService;

    @Operation(summary = "获取仪表板数据", description = "返回核心指标、今日新增、待处理数量、近7天趋势")
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard() {
        return Result.success(adminService.getDashboard());
    }
}
