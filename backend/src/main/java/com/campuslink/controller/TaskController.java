package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.task.*;
import com.campuslink.entity.TaskLog;
import com.campuslink.entity.TaskRating;
import com.campuslink.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@Tag(name = "任务模块", description = "任务发布、接单、完成等接口")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "发布任务")
    @PostMapping("/publish")
    public Result<Map<String, Long>> publishTask(
            @Valid @RequestBody PublishTaskRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Long taskId = taskService.publishTask(userId, request);
        return Result.success("任务发布成功", Map.of("taskId", taskId));
    }

    @Operation(summary = "获取任务列表")
    @GetMapping("/list")
    public Result<PageResult<TaskListResponse>> getTaskList(
            @Parameter(description = "任务类型") @RequestParam(required = false) String taskType,
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "created_at") String sortBy,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        PageResult<TaskListResponse> result = taskService.getTaskList(
                taskType, status, page, pageSize, sortBy, sortOrder
        );
        return Result.success(result);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public Result<TaskResponse> getTaskById(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        TaskResponse task = taskService.getTaskById(id, userId);
        return Result.success(task);
    }

    @Operation(summary = "接单")
    @PostMapping("/{id}/accept")
    public Result<Void> acceptTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.acceptTask(userId, id);
        return Result.success("接单成功", null);
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.completeTask(userId, id);
        return Result.success("任务已完成", null);
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.cancelTask(userId, id);
        return Result.success("任务已取消", null);
    }

    @Operation(summary = "获取我发布的任务")
    @GetMapping("/my/published")
    public Result<PageResult<TaskListResponse>> getMyPublishedTasks(
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        PageResult<TaskListResponse> result = taskService.getMyPublishedTasks(
                userId, status, page, pageSize
        );
        return Result.success(result);
    }

    @Operation(summary = "获取我接受的任务")
    @GetMapping("/my/accepted")
    public Result<PageResult<TaskListResponse>> getMyAcceptedTasks(
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        PageResult<TaskListResponse> result = taskService.getMyAcceptedTasks(
                userId, status, page, pageSize
        );
        return Result.success(result);
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.deleteTask(id, userId);
        return Result.success("任务已删除");
    }

    @Operation(summary = "获取相似任务推荐")
    @GetMapping("/{id}/similar")
    public Result<java.util.List<TaskListResponse>> getSimilarTasks(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "推荐数量") @RequestParam(defaultValue = "5") Integer limit
    ) {
        java.util.List<TaskListResponse> tasks = taskService.getSimilarTasks(id, limit);
        return Result.success(tasks);
    }

    @Operation(summary = "联系发布者", description = "获取发布者信息以便私信联系")
    @PostMapping("/{id}/contact")
    public Result<Map<String, Object>> contactPublisher(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Map<String, Object> result = taskService.getPublisherContact(id, userId);
        return Result.success(result);
    }

    @Operation(summary = "收藏任务")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.favoriteTask(id, userId);
        return Result.success("收藏成功");
    }

    @Operation(summary = "取消收藏任务")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.unfavoriteTask(id, userId);
        return Result.success("取消收藏成功");
    }

    @Operation(summary = "举报任务")
    @PostMapping("/{id}/report")
    public Result<Map<String, Long>> reportTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody Map<String, String> reportData
    ) {
        Long reportId = taskService.reportTask(id, userId, reportData);
        return Result.success("举报成功", Map.of("reportId", reportId));
    }

    /**
     * 开始执行任务
     */
    @Operation(summary = "开始执行任务")
    @PostMapping("/{id}/start")
    public Result<Void> startTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.startTask(id, userId);
        return Result.success("任务已开始");
    }

    /**
     * 提交任务结果
     */
    @Operation(summary = "提交任务结果")
    @PostMapping("/{id}/submit")
    public Result<Void> submitTaskResult(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SubmitTaskResultRequest request
    ) {
        taskService.submitTaskResult(id, userId, request);
        return Result.success("结果已提交,等待发布者确认");
    }

    /**
     * 确认任务完成(发布者审核)
     */
    @Operation(summary = "确认任务完成")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmTaskCompletion(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ConfirmTaskRequest request
    ) {
        taskService.confirmTaskCompletion(id, userId, request);
        return Result.success(request.getApproved() ? "任务已确认完成" : "已要求重新提交");
    }

    /**
     * 放弃任务(接单者)
     */
    @Operation(summary = "放弃任务")
    @PostMapping("/{id}/abandon")
    public Result<Void> abandonTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        taskService.abandonTask(id, userId);
        return Result.success("已放弃任务");
    }

    /**
     * 创建任务评价
     */
    @Operation(summary = "创建任务评价")
    @PostMapping("/{id}/rating")
    public Result<Map<String, Long>> createTaskRating(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        Long ratingId = taskService.createTaskRating(id, userId, request);
        return Result.success("评价成功", Map.of("ratingId", ratingId));
    }

    /**
     * 获取任务操作日志
     */
    @Operation(summary = "获取任务操作日志")
    @GetMapping("/{id}/logs")
    public Result<List<TaskLog>> getTaskLogs(
            @Parameter(description = "任务ID") @PathVariable Long id
    ) {
        List<TaskLog> logs = taskService.getTaskLogs(id);
        return Result.success(logs);
    }

    /**
     * 获取任务评价
     */
    @Operation(summary = "获取任务评价")
    @GetMapping("/{id}/ratings")
    public Result<List<TaskRating>> getTaskRatings(
            @Parameter(description = "任务ID") @PathVariable Long id
    ) {
        List<TaskRating> ratings = taskService.getTaskRatings(id);
        return Result.success(ratings);
    }

}
