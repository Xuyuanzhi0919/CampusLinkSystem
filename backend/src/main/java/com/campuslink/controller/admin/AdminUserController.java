package com.campuslink.controller.admin;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.*;
import com.campuslink.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理接口
 */
@Tag(name = "管理端 - 用户管理")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

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
}
