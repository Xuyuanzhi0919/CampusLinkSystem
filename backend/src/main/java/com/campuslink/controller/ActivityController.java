package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.activity.ActivityParticipantResponse;
import com.campuslink.dto.activity.ActivityResponse;
import com.campuslink.dto.activity.CreateActivityRequest;
import com.campuslink.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器
 */
@Tag(name = "活动模块", description = "活动管理接口")
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "创建活动")
    @PostMapping("/club/{clubId}/create")
    public Result<Long> createActivity(
            @PathVariable Long clubId,
            @Valid @RequestBody CreateActivityRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Long activityId = activityService.createActivity(userId, clubId, request);
        return Result.success("创建成功", activityId);
    }

    @Operation(summary = "获取活动列表")
    @GetMapping("/list")
    public Result<PageResult<ActivityResponse>> getActivityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Integer status,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        PageResult<ActivityResponse> result = activityService.getActivityList(userId, page, pageSize, clubId, status);
        return Result.success(result);
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{activityId}")
    public Result<ActivityResponse> getActivityDetail(
            @PathVariable Long activityId,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        ActivityResponse activityResponse = activityService.getActivityDetail(activityId, userId);
        return Result.success(activityResponse);
    }

    @Operation(summary = "报名活动")
    @PostMapping("/{activityId}/join")
    public Result<Void> joinActivity(
            @PathVariable Long activityId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        activityService.joinActivity(activityId, userId);
        return Result.success("报名成功");
    }

    @Operation(summary = "取消报名")
    @PostMapping("/{activityId}/cancel")
    public Result<Void> cancelJoin(
            @PathVariable Long activityId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        activityService.cancelJoin(activityId, userId);
        return Result.success("取消成功");
    }

    @Operation(summary = "签到")
    @PostMapping("/{activityId}/signin")
    public Result<Void> signIn(
            @PathVariable Long activityId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        activityService.signIn(activityId, userId);
        return Result.success("签到成功");
    }

    @Operation(summary = "获取活动参与者列表")
    @GetMapping("/{activityId}/participants")
    public Result<PageResult<ActivityParticipantResponse>> getParticipants(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        PageResult<ActivityParticipantResponse> result = activityService.getParticipants(activityId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我报名的活动")
    @GetMapping("/my")
    public Result<PageResult<ActivityResponse>> getMyActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        PageResult<ActivityResponse> result = activityService.getMyActivities(userId, page, pageSize);
        return Result.success(result);
    }
}
