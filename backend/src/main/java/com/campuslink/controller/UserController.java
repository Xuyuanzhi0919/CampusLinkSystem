package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.ChangePasswordRequest;
import com.campuslink.dto.CheckInResponse;
import com.campuslink.dto.LikedItemVO;
import com.campuslink.dto.PointsLogVO;
import com.campuslink.dto.user.UpdateProfileRequest;
import com.campuslink.dto.UserStatsVO;
import com.campuslink.dto.UserVO;
import com.campuslink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息查询、修改等接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户统计数据", description = "获取当前用户的统计数据（资源、问答、任务等）")
    @GetMapping("/stats")
    public Result<UserStatsVO> getUserStats(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        UserStatsVO stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        UserVO user = userService.getUserById(userId);
        return Result.success(user);
    }

    @Operation(summary = "获取当前用户资料", description = "获取当前登录用户的个人资料")
    @GetMapping("/profile")
    public Result<UserVO> getUserProfile(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        UserVO userProfile = userService.getUserProfile(userId);
        return Result.success(userProfile);
    }

    @Operation(summary = "更新当前用户资料", description = "更新当前登录用户的个人资料")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @RequestBody UpdateProfileRequest request) {
        UserVO updatedProfile = userService.updateProfile(userId, request);
        return Result.success(updatedProfile);
    }

    @Operation(summary = "获取当前用户积分记录", description = "分页获取当前用户的积分变化记录")
    @GetMapping("/points/log")
    public Result<PageResult<PointsLogVO>> getPointsLog(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PointsLogVO> pointsLog = userService.getPointsLog(userId, page, pageSize);
        return Result.success(pointsLog);
    }

    @Operation(summary = "获取签到状态", description = "查询当前用户今日是否已签到")
    @GetMapping("/check-in/status")
    public Result<Boolean> getCheckInStatus(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        Boolean isCheckedIn = userService.getCheckInStatus(userId);
        return Result.success(isCheckedIn);
    }

    @Operation(summary = "每日签到", description = "用户每日签到，获得积分奖励")
    @PostMapping("/check-in")
    public Result<CheckInResponse> checkIn(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        CheckInResponse response = userService.checkIn(userId);
        return Result.success(response);
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return Result.success("密码修改成功");
    }

    @Operation(summary = "获取我的获赞内容列表", description = "分页返回当前用户上传的资源和回答中被点赞的内容，按点赞数降序")
    @GetMapping("/likes/received")
    public Result<PageResult<LikedItemVO>> getLikedItems(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<LikedItemVO> result = userService.getLikedItems(userId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取用户贡献排行榜", description = "获取积分排行榜前N名用户")
    @GetMapping("/ranking")
    public Result<PageResult<UserVO>> getUserRanking(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        PageResult<UserVO> ranking = userService.getUserRanking(page, pageSize);
        return Result.success(ranking);
    }
}
