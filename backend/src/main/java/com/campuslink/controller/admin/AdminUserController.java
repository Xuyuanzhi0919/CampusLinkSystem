package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.*;
import com.campuslink.entity.PointsLog;
import com.campuslink.mapper.PointsLogMapper;
import com.campuslink.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

/**
 * 管理员用户管理接口
 */
@Tag(name = "管理端 - 用户管理")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;
    private final PointsLogMapper pointsLogMapper;

    @Operation(summary = "用户列表", description = "分页查询用户，支持关键词/角色/状态筛选")
    @GetMapping
    public Result<PageResult<AdminUserVO>> listUsers(
            @Parameter(hidden = true) @ModelAttribute AdminUserQueryRequest req) {
        return Result.success(adminService.listUsers(req));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{userId}")
    public Result<AdminUserVO> getUserDetail(
            @PathVariable Long userId) {
        return Result.success(adminService.getUserDetail(userId));
    }

    @Operation(summary = "封禁/解封用户", description = "status=0 封禁，status=1 解封")
    @PutMapping("/{userId}/status")
    public Result<Void> banUser(
            @PathVariable Long userId,
            @Valid @RequestBody BanUserRequest req) {
        adminService.banUser(userId, req);
        return Result.success(req.getStatus() == 0 ? "封禁成功" : "解封成功");
    }

    @Operation(summary = "修改用户角色", description = "角色：student/teacher/admin")
    @PutMapping("/{userId}/role")
    public Result<Void> setRole(
            @PathVariable Long userId,
            @Valid @RequestBody SetRoleRequest req) {
        adminService.setRole(userId, req);
        return Result.success("角色修改成功");
    }

    @Operation(summary = "手动调整积分", description = "delta 正数增加、负数扣减")
    @PostMapping("/{userId}/points")
    public Result<Void> adjustPoints(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @PathVariable Long userId,
            @Valid @RequestBody AdjustPointsRequest req) {
        adminService.adjustPoints(operatorId, userId, req);
        return Result.success("积分调整成功");
    }

    @Operation(summary = "重置用户密码", description = "随机生成新密码并返回明文")
    @PutMapping("/{userId}/password")
    public Result<Map<String, String>> resetPassword(@PathVariable Long userId) {
        return Result.success(adminService.resetPassword(userId));
    }

    @Operation(summary = "批量封禁/解封用户")
    @PostMapping("/batch-status")
    public Result<Map<String, Integer>> batchSetStatus(
            @Valid @RequestBody BatchStatusRequest req) {
        int count = adminService.batchSetStatus(req);
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "用户内容统计", description = "返回该用户发布的资源/问题/回答/任务数量")
    @GetMapping("/{userId}/stats")
    public Result<Map<String, Long>> getUserStats(@PathVariable Long userId) {
        return Result.success(adminService.getUserStats(userId));
    }

    @Operation(summary = "用户积分流水")
    @GetMapping("/{userId}/points-history")
    public Result<PageResult<PointsLog>> getPointsHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<PointsLog> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<PointsLog> wrapper = new LambdaQueryWrapper<PointsLog>()
                .eq(PointsLog::getUserId, userId)
                .orderByDesc(PointsLog::getCreatedAt);
        Page<PointsLog> result = pointsLogMapper.selectPage(p, wrapper);
        return Result.success(new PageResult<>(
                result.getRecords(), result.getTotal(),
                (long) page, (long) pageSize, result.getPages()
        ));
    }
}
