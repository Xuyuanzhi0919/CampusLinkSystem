package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.notification.NotificationResponse;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 发送通知(管理员)
     */
    @Operation(summary = "发送通知", description = "管理员发送通知,可以发送给指定用户或所有用户")
    @PostMapping("/send")
    public Result<Void> sendNotification(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role,
            @Valid @RequestBody SendNotificationRequest request) {
        // 验证管理员权限(只有教师可以发送通知)
        if (!"teacher".equals(role)) {
            return Result.error(ResultCode.PERMISSION_DENIED);
        }

        notificationService.sendNotification(request);
        return Result.success();
    }

    /**
     * 获取我的通知列表
     */
    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的所有通知")
    @GetMapping("/my")
    public Result<PageResult<NotificationResponse>> getMyNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NotificationResponse> result = notificationService.getMyNotifications(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取未读通知列表
     */
    @Operation(summary = "获取未读通知列表", description = "分页获取当前用户的未读通知")
    @GetMapping("/unread")
    public Result<PageResult<NotificationResponse>> getUnreadNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<NotificationResponse> result = notificationService.getUnreadNotifications(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知总数")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestAttribute("userId") Long userId) {
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    @PutMapping("/{notificationId}/read")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有未读通知标记为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{notificationId}")
    public Result<Void> deleteNotification(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long notificationId) {
        notificationService.deleteNotification(userId, notificationId);
        return Result.success();
    }

    /**
     * 清空所有已读通知
     */
    @Operation(summary = "清空已读通知", description = "清空当前用户的所有已读通知")
    @DeleteMapping("/clear-read")
    public Result<Void> clearReadNotifications(@RequestAttribute("userId") Long userId) {
        notificationService.clearReadNotifications(userId);
        return Result.success();
    }
}
