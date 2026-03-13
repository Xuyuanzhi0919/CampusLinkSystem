package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.Result;
import com.campuslink.dto.notification.SendNotificationRequest;
import com.campuslink.entity.Notification;
import com.campuslink.entity.ScheduledNotice;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ScheduledNoticeMapper;
import com.campuslink.service.AdminService;
import com.campuslink.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员公告/通知接口
 */
@Tag(name = "管理端 - 公告管理")
@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NotificationService notificationService;
    private final AdminService adminService;
    private final ScheduledNoticeMapper scheduledNoticeMapper;

    // ─────────────────── 立即发送 ────────────────────────────

    @Operation(summary = "立即发送公告",
            description = "userId 为空且 role 为空时广播全体；传 role=student/teacher 则按角色发送")
    @PostMapping("/send")
    public Result<Void> send(
            @Parameter(hidden = true) @RequestAttribute("userId") Long operatorId,
            @Valid @RequestBody SendNotificationRequest request) {
        notificationService.sendNotification(request);
        String msg = request.getUserId() != null ? "通知已发送" :
                     request.getRole() != null ? "已向角色 [" + request.getRole() + "] 发送通知" :
                     "公告已广播给全体用户";
        return Result.success(msg);
    }

    // ─────────────────── 发送历史 ────────────────────────────

    @Operation(summary = "公告发送历史", description = "返回最近 20 条去重记录")
    @GetMapping("/history")
    public Result<List<Notification>> getHistory() {
        return Result.success(adminService.getNoticeHistory());
    }

    // ─────────────────── 定时公告 ────────────────────────────

    @Operation(summary = "创建定时公告")
    @PostMapping("/scheduled")
    public Result<Void> createScheduled(@RequestBody ScheduledNotice req) {
        if (req.getScheduledAt() == null || req.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "定时时间必须在当前时间之后");
        }
        req.setStatus(0);
        req.setCreatedAt(LocalDateTime.now());
        scheduledNoticeMapper.insert(req);
        return Result.success("定时公告已创建");
    }

    @Operation(summary = "定时公告列表（待发送）")
    @GetMapping("/scheduled")
    public Result<List<ScheduledNotice>> listScheduled() {
        return Result.success(scheduledNoticeMapper.selectList(
                new LambdaQueryWrapper<ScheduledNotice>()
                        .eq(ScheduledNotice::getStatus, 0)
                        .orderByAsc(ScheduledNotice::getScheduledAt)
        ));
    }

    @Operation(summary = "取消定时公告")
    @PutMapping("/scheduled/{id}/cancel")
    public Result<Void> cancelScheduled(@PathVariable Long id) {
        ScheduledNotice notice = scheduledNoticeMapper.selectById(id);
        if (notice == null || notice.getStatus() != 0) {
            throw new BusinessException(400, "公告不存在或已发送/已取消");
        }
        notice.setStatus(2);
        scheduledNoticeMapper.updateById(notice);
        return Result.success("已取消");
    }
}
