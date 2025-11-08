package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.task.*;
import com.campuslink.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
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
            @Parameter(description = "任务ID") @PathVariable Long id
    ) {
        TaskResponse task = taskService.getTaskById(id);
        return Result.success(task);
    }

    @Operation(summary = "接单")
    @PostMapping("/{id}/accept")
    public Result<Void> acceptTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        taskService.acceptTask(userId, id);
        return Result.success("接单成功", null);
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public Result<Void> completeTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        taskService.completeTask(userId, id);
        return Result.success("任务已完成", null);
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        taskService.cancelTask(userId, id);
        return Result.success("任务已取消", null);
    }

    @Operation(summary = "获取我发布的任务")
    @GetMapping("/my/published")
    public Result<PageResult<TaskListResponse>> getMyPublishedTasks(
            @Parameter(description = "任务状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
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
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PageResult<TaskListResponse> result = taskService.getMyAcceptedTasks(
                userId, status, page, pageSize
        );
        return Result.success(result);
    }
}
